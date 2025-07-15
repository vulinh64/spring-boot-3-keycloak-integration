package com.vulinh.configuration;

import com.vulinh.enums.UserRole;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final ApplicationProperties applicationProperties;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtConverter jwtConverter)
      throws Exception {
    return httpSecurity
        .headers(
            headers ->
                headers
                    .xssProtection(
                        xssConfig ->
                            xssConfig.headerValue(
                                XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                    .contentSecurityPolicy(cps -> cps.policyDirectives("script-src 'self'")))
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(
            sessionManagementConfigurer ->
                sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            customizer ->
                customizer
                    .requestMatchers(asArray(applicationProperties.noAuthUrls()))
                    .permitAll()
                    .requestMatchers(asArray(applicationProperties.adminPrivilegeUrls()))
                    .hasAuthority(UserRole.ROLE_ADMIN.name())
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            customizer ->
                customizer.jwt(
                    jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtConverter)))
        .build();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    var roleHierarchy = "%s > %s".formatted(UserRole.ROLE_ADMIN, UserRole.ROLE_USER);

    log.info("Role hierarchy configured -- {}", roleHierarchy);

    return RoleHierarchyImpl.fromHierarchy(roleHierarchy);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var source = new UrlBasedCorsConfigurationSource();

    var config = new CorsConfiguration();

    config.setAllowCredentials(true);

    var everything = List.of("*");

    config.setAllowedOriginPatterns(everything);
    config.setAllowedHeaders(everything);
    config.setAllowedMethods(everything);

    source.registerCorsConfiguration("/**", config);

    return source;
  }

  private static String[] asArray(List<String> list) {
    return list.toArray(String[]::new);
  }

  // Customized UserDetails object
  public record AuthorizedUserDetails(
      UUID userId,
      String username,
      String email,
      Collection<? extends GrantedAuthority> authorities)
      implements UserDetails {

    public AuthorizedUserDetails {
      authorities = authorities == null ? Collections.emptyList() : authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return authorities;
    }

    // No credentials expose
    @Override
    public String getPassword() {
      return null;
    }

    @Override
    public String getUsername() {
      return username;
    }
  }
}
