package site.code4fun.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import site.code4fun.ApplicationProperties;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.User;
import site.code4fun.model.dto.*;
import site.code4fun.model.mapper.AttachmentMapper;
import site.code4fun.model.mapper.UserMapper;
import site.code4fun.model.request.OTPLoginRequest;
import site.code4fun.model.request.SignInRequest;
import site.code4fun.model.request.VerifyOTPRequest;
import site.code4fun.service.AttachmentService;
import site.code4fun.service.UserService;
import site.code4fun.util.CookieUtils;
import site.code4fun.util.JwtTokenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(AppEndpoints.AUTH_ENDPOINT)
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Lazy
@Slf4j
public class AuthController{

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;
    private final AttachmentService attachmentService;
    private final UserMapper mapper;
    private final AttachmentMapper attachmentMapper;
    private final ApplicationProperties app;

    @Operation(summary = "Common login", description = "Login by userName and password, return access_token")
    @PostMapping(path = "/login", consumes = "application/json")
    @Transactional
    public ResponseEntity<AccessTokenResponseDTO> simpleLogin(@NotNull @RequestBody SignInRequest authenticationRequest, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUserName(), authenticationRequest.getPassword()));
        User userPrincipal = (User) authentication.getPrincipal();
        return doLogin(userPrincipal, request, response);
    }

    @Operation(summary = "Oauth2 login", description = "Login by oauth2 code or idToken, return access_token")
    @PostMapping(path = "/login", consumes = "application/x-www-form-urlencoded")
    @Transactional
    public ResponseEntity<AccessTokenResponseDTO> handleOauth2Code(SignInRequest authenticationRequest, HttpServletRequest request, HttpServletResponse response) {
        User userPrincipal = userService.oauth2Login(authenticationRequest);
        return doLogin(userPrincipal, request, response);
    }

    @Operation(summary = "Logout - delete cookie type http only", description = "Login set cookie is httpOnly can't be delete by js")
    @PostMapping(path = "/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, "isLogin", app.getCookieDomain());
        CookieUtils.deleteCookie(request, response, "token",  app.getCookieDomain());
    }

    @PostMapping(path = "/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public UserDTO register(@RequestBody UserDTO request) {
        User u = mapper.dtoToEntity(request);
        return mapper.entityToDto(userService.create(u));
    }
    @PostMapping(path = "/forgot-password")
    public ResponseEntity<ResponseDTO> forgotPassword(@RequestBody SignInRequest authenticationRequest) {
        userService.forgotPassword(authenticationRequest);
        return ResponseEntity.ok(
                new ResponseDTO(ResponseDTO.Type.success, "Success", 200, null, null)
        );
    }
    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestHeader MultiValueMap<String, String> headers) {
        headers.forEach((key, value) -> log.info("Header '{}' = {}", key, String.join("|", value)));

        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public UserDTO getCurrentUserProfile(){
        return mapper.entityToDto(userService.getCurrentUser());
    }

    @PutMapping("/profile")
    @Transactional
    public UserDTO updateCurrentUserProfile(@RequestBody UserDTO request) {
        User entity = userService.updateProfile(mapper.dtoToEntity(request));
        return mapper.entityToDto(entity);
    }
    @GetMapping("/login-event")
    public Page<LoginEventDTO> getLoginHistory(@RequestParam Map<String, String> mapRequest) {
        return userService.getPagingLoginHistory(mapRequest).map(mapper::loginEventToDTO);
    }

    @GetMapping("/downloads")
    public Page<AttachmentDTO> getAllPaging(@RequestParam Map<String, String> mapRequests) {
        return attachmentService.getPaging(mapRequests).map(attachmentMapper::entityToDto);
    }

    // OTP
    @PostMapping(path = "/otp-login")
    @Transactional
    public ResponseEntity<AccessTokenResponseDTO> otpLogin(@NotNull @RequestBody OTPLoginRequest authenticationRequest, HttpServletRequest request, HttpServletResponse response) {
        User userPrincipal = userService.otpLogin(authenticationRequest);
        return doLogin(userPrincipal, request, response);
    }
    @PostMapping("/send-otp-code")
    @Transactional
    public OtpCode sendOTP(@NotNull @RequestBody OtpCode body) {
        return userService.sendOTP(body.getPhoneNumber());
    }

    @PostMapping("/verify-otp-code")
    @Transactional
    public OtpCode verifyOTP(@RequestBody VerifyOTPRequest body) {
        return userService.verifyOTP(body);
    }

    // Passkey
    @PostMapping("/login-passkey")
    @Transactional
    public ResponseEntity<AssertionRequestWrapper> registerPasskey() {
        return ResponseEntity.ok(userService.createPassKeyLoginRequest());
    }
    @PostMapping("/login-passkey/verify")
    @Transactional
    public ResponseEntity<AccessTokenResponseDTO> authenticatePasskey(@RequestBody Map<String, Object> body, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.verifyLoginPassKey(body);
        return doLogin(user, request, response);
    }

    @PostMapping("/create-passkey")
    @Transactional
    public ResponseEntity<String> createPasskey(){
        return ResponseEntity.ok(userService.createPassKeyRequest());
    }

    @PostMapping("/create-passkey/verify")
    @Transactional
    public void verifyPasskey(@RequestBody Map<String, Object> request) {
        userService.verifyRegisterPassKey(request);
    }

    private ResponseEntity<AccessTokenResponseDTO> doLogin(User userPrincipal, HttpServletRequest request, HttpServletResponse response) {
        AccessTokenResponseDTO responseDTO = jwtTokenUtils.generateToken(userPrincipal);
        userService.saveLoginEvent(request);
        CookieUtils.addLoginCookie(response, responseDTO.getAccessToken(), app.getAdminDomain());
        return ResponseEntity.ok().body(responseDTO);
    }

    @GetMapping(AppEndpoints.INTEGRATIONS_ENDPOINT)
    public ResponseEntity<?> getIntegrations(){
        List<IntegrationDTO> integrationDTOS = new ArrayList<>();
        IntegrationDTO r1 = getGoogleDrive();
        integrationDTOS.add(r1);

        IntegrationDTO r2 = getGitlab();
        integrationDTOS.add(r2);

        IntegrationDTO r3 = new IntegrationDTO();
        r3.setActive(true);
        r3.setName("GHTK");
        r3.setDescription("Connects directly to Vietnam's leading logistics provider. Automates shipping order creation, tracking, and management. Provides real-time delivery status, cost calculation, and address validation. Supports bulk operations with comprehensive reporting and error handling for reliable e-commerce fulfillment.");
        r3.setType("shipping");
        r3.setInstalled(false);
        r3.setImageUrl("https://cdn.haitrieu.com/wp-content/uploads/2022/05/Logo-GHN-Orange.png");
        integrationDTOS.add(r3);

        IntegrationDTO openAi = new IntegrationDTO();
        openAi.setActive(false);
        openAi.setName("OpenAI");
        openAi.setDescription("Harnesses OpenAI's language model for natural text processing within existing workflows. Maintains conversation context, generates diverse content types, extracts structured data, and classifies text. Features configurable parameters for response length and style with built-in content moderation.");
        openAi.setType("AI");
        openAi.setInstalled(false);
        openAi.setImageUrl("https://cdn.brandfetch.io/idR3duQxYl/w/400/h/400/theme/dark/icon.jpeg?c=1bxid64Mup7aczewSAYMX&t=1741166747419");
        integrationDTOS.add(openAi);

        IntegrationDTO gemini = new IntegrationDTO();
        gemini.setActive(false);
        gemini.setName("Gemini");
        gemini.setDescription("Google's multimodal AI integration that processes text, images, and code simultaneously. Delivers enhanced reasoning capabilities for complex tasks, supports multiple languages, and provides contextual understanding. Optimized for real-time analysis with customizable response parameters and secure API authentication.");
        gemini.setType("AI");
        gemini.setInstalled(false);
        gemini.setImageUrl("https://brandlogos.net/wp-content/uploads/2024/04/gemini-logo_brandlogos.net_fwajr.png");
        integrationDTOS.add(gemini);
        return new ResponseEntity<>(integrationDTOS, HttpStatus.OK);
    }

    @NotNull
    private static IntegrationDTO getGitlab() {
        IntegrationDTO r2 = new IntegrationDTO();
        r2.setActive(true);
        r2.setName("GitLab");
        r2.setDescription("Seamless version control integration enabling automated code management across repositories. Provides commit tracking, branch management, and merge operations. Supports webhook-triggered workflows, pull request automation, and conflict resolution. Maintains comprehensive audit trails with secure authentication and configurable access controls.");
        r2.setType("SCM");
        r2.setInstalled(false);
        r2.setImageUrl("https://elstar.themenate.net/img/thumbs/gitlab.png");
        return r2;
    }

    @NotNull
    private static IntegrationDTO getGoogleDrive() {
        IntegrationDTO r1 = new IntegrationDTO();
        r1.setActive(true);
        r1.setName("Google Drive");
        r1.setDescription("Secure connector providing seamless access to Google Drive files and folders. Enables automated document management with real-time synchronization, advanced search, and version control. Supports all file types with OAuth 2.0 security while respecting Google's API limitations.");
        r1.setType("Storage");
        r1.setInstalled(true);
        r1.setImageUrl("https://elstar.themenate.net/img/thumbs/google-drive.png");
        return r1;
    }
}
