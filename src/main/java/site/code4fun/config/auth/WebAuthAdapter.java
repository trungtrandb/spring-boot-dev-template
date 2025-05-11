package site.code4fun.config.auth;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import site.code4fun.model.User;
import site.code4fun.model.WebAuthnCredential;
import site.code4fun.repository.jpa.UserRepository;
import site.code4fun.repository.jpa.WebAuthnCredentialRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Lazy
@Slf4j
@RequiredArgsConstructor
public class WebAuthAdapter implements CredentialRepository {

    private final UserRepository userRepository;
    private final WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        log.debug("getCredentialIdsForUsername {}", username);

        return getRegistrationsByUsername(username).stream()
                .map(
                        registration ->
                                PublicKeyCredentialDescriptor.builder()
                                        .id(registration.getCredentialId())
                                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        log.debug("getUsernameForUserHandle {}", userHandle.getBase64Url());
        return getRegistrationsByUserHandle(userHandle).stream()
                .findAny()
                .map(WebAuthnCredential::getUserName);
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        log.debug("getUserHandleForUsername {}", username);
        return getRegistrationsByUsername(username).stream()
                .findAny()
                .map(WebAuthnCredential::getUserHandle);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        log.debug( "lookup credential ID: {}, user handle: {}", credentialId.getBase64(), userHandle.getBase64());
        Collection<WebAuthnCredential> lst = webAuthnCredentialRepository.findByCredentialId(credentialId.getBase64());

        return lst.stream().findAny().map(
                                registration ->
                                        RegisteredCredential.builder()
                                                .credentialId(credentialId)
                                                .userHandle(userHandle)
                                                .publicKeyCose(registration.getPublicKeyCose())
                                                .signatureCount(registration.getSignatureCount())
                                                .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        log.debug( "lookupAll credential ID: {}", credentialId.getBase64());
        Collection<WebAuthnCredential> lst = webAuthnCredentialRepository.findByCredentialId(credentialId.getBase64());
        return lst.stream()
                .map(reg ->  RegisteredCredential.builder()
                                        .credentialId(credentialId)
                                        .userHandle(reg.getUserHandle())
                                        .publicKeyCose(reg.getPublicKeyCose())
                                        .signatureCount(reg.getSignatureCount())
                                        .build())
                .collect(Collectors.toSet());
    }

    @SneakyThrows
    public void addRegistrationByUsername(String username, WebAuthnCredential reg) {
        log.debug( "addRegistrationByUsername: username {}, CredentialRegistration {}", username, reg.toString());

        Optional<User> userOptional = userRepository.findByUsername(username);
        userOptional.ifPresent(user -> user.getCredentials().add(reg));
    }

    @SneakyThrows
    public Collection<WebAuthnCredential> getRegistrationsByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()){
            return userOptional.get().getCredentials();
        }
        return new HashSet<>();
    }

    public Collection<WebAuthnCredential> getRegistrationsByUserHandle(ByteArray userHandle) {
        return webAuthnCredentialRepository.getRegistrationsByUserHandle(userHandle.getBase64());
    }
}