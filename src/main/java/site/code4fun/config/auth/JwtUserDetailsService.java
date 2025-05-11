package site.code4fun.config.auth;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.code4fun.constant.Oauth2Provider;
import site.code4fun.model.User;
import site.code4fun.repository.jpa.UserRepository;

import java.util.Map;
import java.util.Optional;

import static site.code4fun.util.RandomUtils.random;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService
		extends DefaultOAuth2UserService // Oauth2 Login
		implements  UserDetailsService  { // Username/Password login

	private final UserRepository userRepository;

	/**
	 * An implementation of an {@link UserDetailsService} that supports basic login.
	 * @author TrungTQ
	 */
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		Optional<site.code4fun.model.User> user = userRepository.findByUsername(userName);
		return user.orElseThrow(() -> new UsernameNotFoundException(userName));
	}


	/**
	 * An implementation of an {@link OAuth2UserService} that supports standard OAuth 2.0 Provider's.
	 * @author TrungTQ
	 * @see DefaultOAuth2User
	 */
	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
		OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
		return processOAuth2User(oAuth2UserRequest, oAuth2User.getAttributes());
	}

	private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes) {
		Oauth2Provider provider = Oauth2Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId());
		OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, attributes);
		if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
			throw new RuntimeException("Email not found from OAuth2 provider");
		}

		Optional<User> userOptional = userRepository.findByUsername(oAuth2UserInfo.getEmail());
		User user;
		if(userOptional.isPresent()) {
			user = userOptional.get();
			if(!user.getOauth2Provider().equals(Oauth2Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
				throw new RuntimeException("Looks like you're signed up with " +
						user.getOauth2Provider() + " account. Please use your " + user.getOauth2Provider() +
						" account to login.");
			}
			user = updateExistingUser(user, oAuth2UserInfo);
		} else {
			user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
		}
		return OAuth2Principal.fromUser(user, attributes);
	}

	private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
		User user = new User();

		user.setOauth2Provider(Oauth2Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
		user.setOauth2Id(oAuth2UserInfo.getId());
		user.setLastName(oAuth2UserInfo.getName());
		user.setEmail(oAuth2UserInfo.getEmail());
		user.setAvatar(oAuth2UserInfo.getImageUrl());
		user.setPassword(random(8));
		user.setUsername(user.getEmail());
		return userRepository.save(user);
	}

	private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
		existingUser.setLastName(oAuth2UserInfo.getName());
		existingUser.setAvatar(oAuth2UserInfo.getImageUrl());
		return userRepository.save(existingUser);
	}
}
