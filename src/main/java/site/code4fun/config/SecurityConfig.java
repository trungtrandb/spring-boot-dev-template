package site.code4fun.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import site.code4fun.ApplicationProperties;
import site.code4fun.config.auth.*;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String PUBLIC_LOGIN_ENTRY_POINT = "/auth/**";
    private static final String[] NON_TOKEN_BASED_AUTH_ENTRY_POINTS = new String[]{"/index.html", "/assets/**", "/static/**", "/webjars/**", "/api/license/**", "/api/images/public/**", "/.well-known/**"};
    private static final String USER_ENTRY_POINT = "/users/**";
    private static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/admin/**";
    private static final String[] WS_ENTRY_POINT = new String[]{"/api/ws/**", "/ws/**"};

    private final JwtUserDetailsService userDetailsService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final ApplicationProperties applicationProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter,
                                    OAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository,
                                    RateLimitProcessingFilter rateLimitProcessingFilter) throws Exception {
        http.headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("form-action 'self'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOriginPatterns(Arrays.asList(applicationProperties.getCorsDomainArray()));
                    configuration.setAllowedMethods(List.of("*"));
                    configuration.setAllowedHeaders(List.of("*"));
                    configuration.setAllowCredentials(true);
                    return configuration;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(config -> config
                        .requestMatchers(NON_TOKEN_BASED_AUTH_ENTRY_POINTS).permitAll() // static resources
                        .requestMatchers(WS_ENTRY_POINT).permitAll() // Protected WebSocket API End-points
                        .requestMatchers(PUBLIC_LOGIN_ENTRY_POINT).permitAll()  // Public for login/logout end-point
                        .requestMatchers(USER_ENTRY_POINT).authenticated() // Protected API End-points
                        .requestMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).hasAuthority("ROLE_ADMIN") // Protected API End-points
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitProcessingFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(login -> login
                        .authorizationEndpoint(config -> config
                                .baseUri("/oauth2/authorize")
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        ).redirectionEndpoint(config -> config
                                .baseUri("/oauth2/callback/*")
                        )
                        .userInfoEndpoint(userInfoEndpointConfig ->
                                userInfoEndpointConfig.userService(userDetailsService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );
        return http.build();
    }

    @Bean
    public DefaultWebSecurityExpressionHandler securityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_DELETE \n ROLE_DELETE > ROLE_WRITE \n ROLE_WRITE > ROLE_READ"));
        return expressionHandler;
    }
}