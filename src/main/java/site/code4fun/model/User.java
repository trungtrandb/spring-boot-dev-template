package site.code4fun.model;

import com.google.api.client.auth.oauth2.StoredCredential;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.code4fun.constant.AppConstants;
import site.code4fun.constant.Oauth2Provider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity class representing a user in the system.
 * Implements UserDetails for Spring Security integration.
 */
@Entity
@Data
@Table(name = AppConstants.TABLE_PREFIX + "user")
@EqualsAndHashCode(exclude = {"roles", "credentials"})
@ToString(exclude = {"roles", "credentials"})
public class User extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(max = 100)
    private String email;

    @Column(length = 15)
    private String gender;

    @Size(max = 200)
    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String title;

    private Date birthday;
    private boolean enabled;
    
    @Size(max = 20)
    private String identityCard;

    @Enumerated(EnumType.STRING)
    private Oauth2Provider oauth2Provider;

    private String oauth2Id;
    private String token;
    private String refreshToken;
    private Long tokenExpire;
    
    @Size(max = 10)
    private String langKey;

    @Column(length = 5000)
    private String challenge;

    @Column(length = 500)
    private String avatar;

    @Column(length = 20)
    private String resetKey;
    private Date resetDate;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = AppConstants.TABLE_PREFIX + "users_roles")
    private Set<Role> roles = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WebAuthnCredential> credentials = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private ShiftEntity shift;

    @Transient
    private Collection<GrantedAuthority> authorities;

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = roles.stream()
                .flatMap(role -> {
                    List<GrantedAuthority> roleAuthorities = new ArrayList<>();
                    roleAuthorities.add(new SimpleGrantedAuthority(role.getName()));
                    role.getPrivileges().forEach(privilege -> 
                        roleAuthorities.add(new SimpleGrantedAuthority(privilege)));
                    return roleAuthorities.stream();
                })
                .collect(Collectors.toList());
        }
        return authorities;
    }

    public StoredCredential toStoredCredential() {
        return new StoredCredential()
            .setAccessToken(this.token)
            .setRefreshToken(this.refreshToken)
            .setExpirationTimeMilliseconds(this.tokenExpire);
    }

    public void addRole(Role role) {
        if (role != null) {
            roles.add(role);
            authorities = null; // Reset cached authorities
        }
    }

    public void removeRole(Role role) {
        if (role != null) {
            roles.remove(role);
            authorities = null; // Reset cached authorities
        }
    }

    public boolean isSystemAdmin() {
        return roles.stream()
            .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role.getName()));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}