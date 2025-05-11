package site.code4fun.repository.jpa;

import site.code4fun.model.WebAuthnCredential;

import java.util.Collection;
import java.util.List;

public interface WebAuthnCredentialRepository extends BaseRepository<WebAuthnCredential, Long>{
	List<WebAuthnCredential> getRegistrationsByUserHandle(String userHandleBase64);

	Collection<WebAuthnCredential> findByCredentialId(String baseId);
}
