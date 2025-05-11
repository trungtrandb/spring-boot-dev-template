package site.code4fun.model;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import site.code4fun.constant.AppConstants;

import java.io.Serializable;
import java.util.Base64;

/**
 * Entity class representing WebAuthn credentials for user authentication.
 * This class stores the necessary information for WebAuthn authentication including
 * user identity, credential ID, and public key.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "web_auth_credential")
public class WebAuthnCredential implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotBlank
    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    private String userHandle;

    @NotBlank
    @Column(nullable = false)
    private String userName;

    @NotBlank
    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    private String credentialId;

    @NotNull
    @Column(columnDefinition = "BLOB", nullable = false)
    @Getter(AccessLevel.NONE)
    private byte[] publicKeyCose;

    @Column(nullable = false)
    private long signatureCount;

    /**
     * Sets the user identity information from a UserIdentity object.
     * @param userIdentity The UserIdentity object containing user information
     */
    public void setUserIdentity(UserIdentity userIdentity) {
        if (userIdentity == null) {
            throw new IllegalArgumentException("UserIdentity cannot be null");
        }
        setUserHandle(userIdentity.getId().getBase64());
        setUserName(userIdentity.getName());
    }

    /**
     * Gets the credential ID as a ByteArray.
     * @return ByteArray representation of the credential ID
     */
    public ByteArray getCredentialId() {
        if (credentialId == null) {
            throw new IllegalStateException("Credential ID is not set");
        }
        return new ByteArray(Base64.getDecoder().decode(credentialId));
    }

    /**
     * Gets the public key COSE as a ByteArray.
     * @return ByteArray representation of the public key COSE
     */
    public ByteArray getPublicKeyCose() {
        if (publicKeyCose == null) {
            throw new IllegalStateException("Public key COSE is not set");
        }
        return new ByteArray(publicKeyCose);
    }

    /**
     * Sets the credential information from a RegisteredCredential object.
     * @param credential The RegisteredCredential object containing credential information
     */
    public void setCredential(RegisteredCredential credential) {
        if (credential == null) {
            throw new IllegalArgumentException("RegisteredCredential cannot be null");
        }
        setCredentialId(credential.getCredentialId().getBase64());
        setPublicKeyCose(credential.getPublicKeyCose().getBytes());
        setSignatureCount(credential.getSignatureCount());
    }

    /**
     * Gets the user handle as a ByteArray.
     * @return ByteArray representation of the user handle
     */
    public ByteArray getUserHandle() {
        if (userHandle == null) {
            throw new IllegalStateException("User handle is not set");
        }
        return new ByteArray(Base64.getDecoder().decode(userHandle));
    }
}
