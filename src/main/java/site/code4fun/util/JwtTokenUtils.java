package site.code4fun.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import site.code4fun.ApplicationProperties;
import site.code4fun.exception.ServiceException;
import site.code4fun.model.Role;
import site.code4fun.model.User;
import site.code4fun.model.dto.AccessTokenResponseDTO;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUtils implements Serializable {

	private final ApplicationProperties properties;
	public static final int JWT_TOKEN_VALIDITY = 5 * 3600;

	private static final String KEY_AUTHORITIES = "authorities";
	private static final String KEY_ROLES = "roles";

	private static final ObjectMapper mapper = new ObjectMapper();

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}
	
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}

	public boolean isValidToken(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.after(new Date());
	}

	public AccessTokenResponseDTO generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", user.getId());
		claims.put("name", user.getLastName() + " " + user.getFirstName());
		claims.put(KEY_ROLES, user.getRoles());
		claims.put(KEY_AUTHORITIES, user.getAuthorities());
		claims.put("avatar", user.getAvatar());
		
		String token =  Jwts.builder()
				.claims(claims)
				.subject(user.getUsername())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(getExpiration())
				.signWith(getKey())
				.compact();
		
		return AccessTokenResponseDTO.builder()
				.accessToken(token)
				.build();
	}

	public static Date getExpiration(){
		return new Date(System.currentTimeMillis() + getMaxAge());
	}

	public static int getMaxAge(){
		return JWT_TOKEN_VALIDITY * 1000;
	}

	public User getUserPrincipalFromToken(String token) {
		Set<Role> roles = new HashSet<>();
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();

		Claims claims;
		try {
			claims = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
		} catch (JwtException e) {
			// Handle token parsing exception (e.g., log the error, throw a custom exception)
			throw new ServiceException("Invalid JWT token");
		}

		Optional.ofNullable(claims.get(KEY_ROLES))
				.ifPresent(role -> roles.addAll(mapper.convertValue(role, new TypeReference<Set<Role>>() {})));

		Optional.ofNullable(claims.get(KEY_AUTHORITIES))
				.map(authority -> mapper.convertValue(authority, new TypeReference<List<Map<String, String>>>() {}))
				.ifPresent(lst -> {
					List<SimpleGrantedAuthority> tempAuthorities = lst.stream().map(item -> new SimpleGrantedAuthority(item.get("authority"))).toList();
					authorities.addAll(tempAuthorities);
				});
		String avt = claims.get("avatar") != null ? claims.get("avatar").toString() : "";
		User user = new User();
		user.setUsername(claims.getSubject());
		user.setId(((Integer) claims.get("id")).longValue());
		user.setAuthorities(new ArrayList<>(authorities));
		user.setRoles(roles);
		user.setAvatar(avt);

		return user;
	}



	private SecretKey getKey(){
		return Keys.hmacShaKeyFor(properties.getJwtSigningKey().getBytes());
	}
}
