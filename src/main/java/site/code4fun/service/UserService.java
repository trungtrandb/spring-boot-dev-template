package site.code4fun.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.internal.util.CollectionsUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.code4fun.ApplicationProperties;
import site.code4fun.config.auth.WebAuthAdapter;
import site.code4fun.constant.Oauth2Provider;
import site.code4fun.constant.SearchOperator;
import site.code4fun.exception.NotFoundException;
import site.code4fun.exception.ServiceException;
import site.code4fun.exception.ValidationException;
import site.code4fun.model.LoginEventEntity;
import site.code4fun.model.OtpCodeEntity;
import site.code4fun.model.User;
import site.code4fun.model.WebAuthnCredential;
import site.code4fun.model.dto.AssertionRequestWrapper;
import site.code4fun.model.dto.OtpCode;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.model.mapper.OtpCodeMapper;
import site.code4fun.model.request.OTPLoginRequest;
import site.code4fun.model.request.SignInRequest;
import site.code4fun.model.request.VerifyOTPRequest;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.LoginEventRepository;
import site.code4fun.repository.jpa.OtpCodeRepository;
import site.code4fun.repository.jpa.RoleRepository;
import site.code4fun.repository.jpa.UserRepository;
import site.code4fun.service.email.EmailService;
import site.code4fun.service.google.GoogleOauth2Properties;
import site.code4fun.util.RandomUtils;
import site.code4fun.util.RegexUtils;
import site.code4fun.util.SecurityUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static site.code4fun.util.DateTimeUtils.getFirstDateOfMonth;
import static site.code4fun.util.DateTimeUtils.getLastDateOfMonth;
import static site.code4fun.util.RandomUtils.generateRandom;
import static site.code4fun.util.RandomUtils.random;
import static site.code4fun.util.RegexUtils.isValidEmail;
import static site.code4fun.util.UrlParserUtils.getClientIpAddress;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class UserService extends AbstractBaseService<User, Long> {

    @Getter(AccessLevel.PROTECTED)
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TwilioService twilioService;
    private final OtpCodeRepository otpCodeRepository;
    private final GoogleOauth2Properties googleOauth2Properties;
    private final ApplicationProperties properties;
    private final OtpCodeMapper otpCodeMapper;
    private final WebAuthAdapter userStorage;
    private final RedisTemplate<String, String> redisTemplate;
    private final LoginEventRepository loginEventRepository;
    private final IpInfoService ipInfoService;
    private final Gson gson;

    private final Map<String, String> mappingHeader = Map.ofEntries(
            Map.entry("id", "Id"),
            Map.entry("lastName", "Last Name"),
            Map.entry("firstName", "First Name"),
            Map.entry("username", "Username"),
            Map.entry("email", "Email"),
            Map.entry("address", "Address")
    );

    @Override
    protected Map<String, String> getMappingHeader() {
        return mappingHeader;
    }

    @Override
    public User create(@NotNull User user) {
        user.setRoles(new HashSet<>()); // can't self grant permission
        validateParamAndExistEmail(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUsername(user.getEmail());
        user.setEnabled(true);
        getRepository().save(user);
        grantPermission(user);
        return user;
    }

    @Override
    public void delete(Long id) {
        if (Objects.equals(SecurityUtils.getUserId(), id)) {
            throw new ServiceException("Can't self delete");
        }
        loginEventRepository.deleteByCreatedBy_id(id);
        super.delete(id);
    }

    @Override
    protected Specification<User> createSpecification(List<SearchCriteria> criteriaList, String queryString) {
        Specification<User> spec = null;

        if (isNotBlank(queryString)) {
            spec = new SearchSpecification<>(new SearchCriteria("email", SearchOperator.EQUAL, queryString));
            spec = spec.or(new SearchSpecification<>(new SearchCriteria("firstName", SearchOperator.EQUAL, queryString)));
            spec = spec.or(new SearchSpecification<>(new SearchCriteria("lastName", SearchOperator.EQUAL, queryString)));
        }

        for (SearchCriteria criteria : criteriaList) {
            if ("roles".equalsIgnoreCase(criteria.getKey())) {
                criteria.setOperation(SearchOperator.IN);
            }
            spec = (spec == null) ? new SearchSpecification<>(criteria) : spec.and(new SearchSpecification<>(criteria));
        }

        return spec;
    }

    public User getCurrentUser() { // Force load from database, not in JWT token
        User userPrincipal = SecurityUtils.getUser();
        if (userPrincipal != null) {
            return getRepository().findById(userPrincipal.getId())
                    .orElseThrow(() -> new AccessDeniedException("Not found"));
        }
        throw new ServiceException("User not logged in");
    }

    public User oauth2Login(@NotNull SignInRequest loginRequest) {
        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        if ("facebook".equalsIgnoreCase(loginRequest.getProvider())) {// next auth
            return null; //TODO
        } else if (isNotBlank(loginRequest.getIdToken())) { //Using Google Login on IOS already exchange code for id token
            tokenResponse.setIdToken(loginRequest.getIdToken());
            tokenResponse.setFactory(GsonFactory.getDefaultInstance());
        } else {
            tokenResponse = googleOauth2Properties.exchangeToken(loginRequest.getCode(), loginRequest.getRedirectUrl());
        }
        return loginWithGoogle(tokenResponse);
    }

    public User otpLogin(OTPLoginRequest request) {
        Optional<OtpCodeEntity> otp = otpCodeRepository.findById(request.getOtpId());
        if (otp.isPresent()) {
            Optional<User> userOptional = getRepository().findByEmailOrPhone("", otp.get().getPhoneNumber());
            if (userOptional.isPresent()) {
                return userOptional.get();
            } else {
                User user = new User();
                user.setLastName(request.getName());
                user.setPassword(random(8));
                user.setEmail(request.getEmail());
                user.setPhone(otp.get().getPhoneNumber());
                user.setUsername(otp.get().getPhoneNumber());

                return create(user);
            }
        }
        throw new NotFoundException();
    }

    public void updateEnable(Long userId) {
        User u = getById(userId);
        u.setEnabled(!u.isEnabled());
        getRepository().save(u);
    }

    @SneakyThrows
    public User loginWithGoogle(@NotNull GoogleTokenResponse tokenResponse) {
        GoogleIdToken.Payload googleIdToken = tokenResponse.parseIdToken().getPayload();
        Optional<User> userOptional = getRepository().findByOauth2IdAndOauth2Provider(
                googleIdToken.getSubject(),
                Oauth2Provider.google);

        if (userOptional.isPresent()) {
            if (userOptional.get().isEnabled()) {
                User u = userOptional.get();
                u.setToken(tokenResponse.getAccessToken());
                u.setRefreshToken(tokenResponse.getRefreshToken());
                return getRepository().save(u);
            } else {
                throw new ValidationException("User is disable");
            }
        }

        User user = new User();
        user.setToken(tokenResponse.getAccessToken());
        user.setRefreshToken(tokenResponse.getAccessToken());
        user.setOauth2Provider(Oauth2Provider.google);
        user.setOauth2Id(googleIdToken.getSubject());
        user.setAvatar(googleIdToken.get("picture").toString());
        user.setLastName(googleIdToken.get("name").toString());
        user.setPassword(random(8));
        user.setEmail(googleIdToken.getEmail());
        user.setUsername(googleIdToken.getEmail());

        return create(user);
    }

    public User updateProfile(@NotNull User req) {
        User u = getCurrentUser();
        return update(u.getId(), req);
    }

    @Override
    public User update(Long id, User req) {
        validateParamAndExistEmail(req);

        User u = getById(id);

        if (isNotBlank(req.getAvatar())) {
            u.setAvatar(req.getAvatar());
        }

        if (isNotBlank(req.getLangKey())) {
            u.setLangKey(req.getLangKey());
        }

        if (isNotBlank(req.getTitle())) {
            u.setTitle(req.getTitle());
        }

        if (isNotBlank(req.getAddress())) {
            u.setAddress(req.getAddress());
        }

        if (isNotBlank(req.getPassword())) {
            u.setPassword(passwordEncoder.encode(req.getPassword().trim()));
        }

        // Only allow setRoles if currentUser has ADMIN_ROLE
        if (SecurityUtils.getUser() != null
                && !Objects.equals(u.getId(), getCurrentUser().getId())
                && SecurityUtils.getUser().getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role.getName()))) {
            u.setRoles(new HashSet<>());
            if (CollectionsUtils.hasItems(req.getRoles())) {
                req.getRoles().forEach(us -> u.addRole(roleRepository.getReferenceById(us.getId())));
            }

        }
        u.setPhone(req.getPhone());
        u.setLastName(req.getLastName());
        u.setFirstName(req.getFirstName());
        u.setShift(req.getShift());
        u.setBirthday(req.getBirthday());
        u.setGender(req.getGender());
        return getRepository().save(u);
    }

    public void forgotPassword(@NotNull SignInRequest authenticationRequest) {
        if (isBlank(authenticationRequest.getUserName())) throw new ValidationException("Username must not be null");
        Optional<User> userO = getRepository().findByEmailOrPhone(authenticationRequest.getUserName(), authenticationRequest.getUserName());
        if (userO.isEmpty()) throw new NotFoundException("User not found");
        userO.ifPresent(user -> {
            if (isValidEmail(authenticationRequest.getUserName())) {
                emailService.sendPasswordResetMail(user);
            } else if (isNotBlank(user.getPhone())) {
                sendOTP(user.getPhone());
            } else {
                throw new ValidationException("Email and phone is invalid");
            }
        });
    }

    @SneakyThrows
    public OtpCode sendOTP(String phoneNumber) {
        String randomCode = RandomUtils.random(6);

        OtpCodeEntity entity = new OtpCodeEntity();
        entity.setCode(randomCode);
        entity.setPhoneNumber(phoneNumber);
        entity.setProvider(TwilioService.class.toString());

        String messageId = twilioService.send(phoneNumber, randomCode);
        entity.setMessageId(messageId);
        entity.setSuccess(true);
        entity = otpCodeRepository.save(entity);
        return otpCodeMapper.entityToDto(entity);
    }

    public OtpCode verifyOTP(VerifyOTPRequest body) {
        OtpCode otpCode = new OtpCode();
        otpCode.setMessage("Not valid");

        Optional<OtpCodeEntity> entityO = otpCodeRepository.findById(body.getOtpId());
        if (entityO.isPresent() && StringUtils.equalsIgnoreCase(body.getCode(), entityO.get().getCode())) {
            otpCode.setMessage("Verify success");
            otpCode.setSuccess(true);
            otpCodeRepository.delete(entityO.get());
        }
        return otpCode;
    }

    public Object getStatistic() {
        String valueField = "value";
        String growField = "growShrink";

        Map<String, Map<String, Object>> mapRes = new HashMap<>();
        long totalUser = getRepository().count();
        long countCustomer = getRepository().countByRoles_nameNot("ROLE_ADMIN");

        mapRes.put("totalCustomers", Map.ofEntries(
                Map.entry(valueField, countCustomer),
                Map.entry(growField, countCustomer * 100 / totalUser))
        );

        long countActive = getRepository().countByEnabledIsTrue();
        mapRes.put("activeCustomers", Map.ofEntries(
                Map.entry(valueField, countActive),
                Map.entry(growField, countActive * 100 / totalUser))
        );

        long countNew = getRepository().countByCreatedBetween(getFirstDateOfMonth(), getLastDateOfMonth());
        mapRes.put("newCustomers", Map.ofEntries(
                Map.entry(valueField, countNew),
                Map.entry(growField, countNew * 100 / totalUser))
        );
        return mapRes;
    }

    @SneakyThrows
    public String createPassKeyRequest() {
        PublicKeyCredentialCreationOptions creationOptions = getRequest();
        getCurrentUser().setChallenge(creationOptions.toJson());
        return creationOptions.toCredentialsCreateJson();
    }

    @SneakyThrows
    public AssertionRequestWrapper createPassKeyLoginRequest() {

        AssertionRequestWrapper request = new AssertionRequestWrapper(
                generateRandom(32),
                getRelyParty().startAssertion(StartAssertionOptions.builder().build()));
        redisTemplate.opsForValue().set(request.getRequestId(), request.getRequest().toJson(), 5, TimeUnit.MINUTES);
        return request;
    }

    @SneakyThrows
    public void verifyRegisterPassKey(Map<String, Object> mapReq) {
        String publicKeyCredentialJson = gson.toJson(mapReq);

        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson);

        String pc = getCurrentUser().getChallenge();
        PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.fromJson(pc);
        RegistrationResult registration = getRelyParty().finishRegistration(FinishRegistrationOptions.builder()
                .request(request)
                .response(pkc)
                .build());

        addRegistration(request.getUser(), registration);
    }

    @SneakyThrows
    public User verifyLoginPassKey(Map<String, Object> request) {
        String requestId = (String) request.get("requestId");
        request.remove("requestId");
        String pubkeyJson = gson.toJson(request);

        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pubKey = PublicKeyCredential.parseAssertionResponseJson(pubkeyJson);
        AssertionRequest req = AssertionRequest.fromJson(redisTemplate.opsForValue().getAndDelete(requestId));
        AssertionResult result = getRelyParty().finishAssertion(FinishAssertionOptions.builder()
                .request(req)  // The PublicKeyCredentialRequestOptions from startAssertion above
                .response(pubKey)
                .build());

        if (isNotBlank(result.getUsername())) {
            Optional<User> userOptional = getRepository().findByUsername(result.getUsername());
            if (userOptional.isPresent()) {
                return userOptional.get();
            }
        }
        throw new NotFoundException("User not found");
    }

    private PublicKeyCredentialCreationOptions getRequest() {
        User user = getCurrentUser();
        var userIdentity = UserIdentity.builder()
                .name(user.getEmail())
                .displayName(user.getLastName() + " " + user.getFirstName())
                .id(generateRandom(32))
                .build();

        return getRelyParty().startRegistration(
                StartRegistrationOptions.builder()
                        .user(userIdentity)
                        .build());
    }

    @SneakyThrows
    private RelyingParty getRelyParty() {
        String domain = RegexUtils.parseHost(properties.getAdminDomain());

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(domain)
                .name(properties.getAppName())
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(userStorage)
                .allowOriginPort(true)
                .allowOriginSubdomain(true)
                .build();

    }

    private void addRegistration(UserIdentity userIdentity, RegistrationResult result) {
        RegisteredCredential credential = RegisteredCredential.builder()
                .credentialId(result.getKeyId().getId())
                .userHandle(userIdentity.getId())
                .publicKeyCose(result.getPublicKeyCose())
                .signatureCount(result.getSignatureCount())
                .build();

        WebAuthnCredential reg = new WebAuthnCredential();
        reg.setUserIdentity(userIdentity);
        reg.setCredential(credential);

        log.debug("Adding registration nickname: {}, credential: {}", userIdentity, credential);
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
    }

    private void grantPermission(User user) {
        roleRepository.findByNameContains("READ").ifPresent(user::addRole);
    }

    private void validateParamAndExistEmail(User user) {
        Optional<User> u = getRepository().findByUsername(user.getUsername());

        // create
        if (user.getId() == null) {
            if (u.isPresent())
                throw new ValidationException("User with email already existed");

            if (isBlank(user.getPassword()))
                throw new ValidationException("Password must not be null");

            //update
        } else if (u.isPresent() && !Objects.equals(u.get().getId(), user.getId())) {
            throw new ValidationException("User with email already existed");
        }
    }

    public void saveLoginEvent(HttpServletRequest request) {
        String userIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        LoginEventEntity loginEventEntity = new LoginEventEntity();
        loginEventEntity.setDeviceDetails(userAgent);
        loginEventEntity.setIpAddress(userIp);
        loginEventEntity.setLoginTime(LocalDateTime.now());
        loginEventEntity.setLocation(ipInfoService.getIpInfo(userIp));

        loginEventRepository.save(loginEventEntity);
    }

    public Page<LoginEventEntity> getPagingLoginHistory(Map<String, String> mapRequest) {
        mapRequest.put("sort", "created");
        mapRequest.put("sortDir", "DESC");
        Pageable pageReq = buildPageRequest(mapRequest);
        Specification<LoginEventEntity> spec;

        if (!SecurityUtils.isAdmin()) {
            spec = new SearchSpecification<>(new SearchCriteria("createdBy", SearchOperator.IN, mapRequest));
        } else {
            spec = new SearchSpecification<>(new SearchCriteria("createdBy", SearchOperator.IN, mapRequest.get("createdBy")));
        }


        return loginEventRepository.findAll(spec, pageReq);
    }
}



