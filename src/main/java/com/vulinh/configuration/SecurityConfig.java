package com.vulinh.configuration;

import com.vulinh.enums.UserRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
        .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
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

  private static CorsConfigurationSource corsConfigurationSource() {
    var corsConfigurationSource = new UrlBasedCorsConfigurationSource();

    var corsConfiguration = new CorsConfiguration();

    corsConfiguration.setAllowCredentials(true);

    var everything = List.of("*");

    corsConfiguration.setAllowedOriginPatterns(everything);
    corsConfiguration.setAllowedHeaders(everything);
    corsConfiguration.setAllowedMethods(everything);

    corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

    return corsConfigurationSource;
  }

  private static String[] asArray(List<String> list) {
    return list.toArray(String[]::new);
  }
}
