package site.code4fun.repository.jpa;

import site.code4fun.constant.Oauth2Provider;
import site.code4fun.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long>{
	Optional<User> findByUsername(String userName);
	Optional<User> findByOauth2IdAndOauth2Provider(String oauthId, Oauth2Provider oAuthProvider);
	long countByEnabledIsTrue();
	long countByCreatedBetween(LocalDate date1, LocalDate date2);
	long countByRoles_nameNot(String role);
	Optional<User> findByEmailOrPhone(String str, String str2);
	List<User> findAllByRoles_idIn(Collection<Long> ids);

    List<User> findAllByShift_Id(Long shiftId);
	User findByEmailContainsIgnoreCase(String email);
}
