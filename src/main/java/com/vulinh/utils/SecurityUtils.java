package com.vulinh.utils;

import com.vulinh.configuration.SecurityConfig;
import com.vulinh.exception.AuthorizationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtils {

  public static SecurityConfig.AuthorizedUserDetails getUserDetails() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new AuthorizationException("Empty authentication");
    }

    var userPrincipal = authentication.getPrincipal();

    if (!(userPrincipal instanceof SecurityConfig.AuthorizedUserDetails details)) {
      throw new AuthorizationException(
          "Invalid user details object, expected class [%s]"
              .formatted(SecurityConfig.AuthorizedUserDetails.class.getName()));
    }

    return details;
  }
}
