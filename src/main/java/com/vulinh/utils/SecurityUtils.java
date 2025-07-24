package com.vulinh.utils;

import com.vulinh.configuration.AuthorizedUserDetails;
import com.vulinh.exception.AuthorizationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/** Utility class meant for quickly retrieve authenticated user info */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtils {

  // Stupid engineers who DARE reparse the JWT (authentication.getCredentials()) AGAIN
  // should be axed and never be allowed to work in this industry
  // Yes, some people in a banking system did not even now how to use SecurityContextHolder

  /**
   * Retrieve the details of the current authenticated user.
   *
   * @return an UserDetails object
   */
  public static UserDetails getUserDetails() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new AuthorizationException("Empty authentication");
    }

    var userPrincipal = authentication.getPrincipal();

    if (!(userPrincipal instanceof AuthorizedUserDetails details)) {
      throw new AuthorizationException(
          "Invalid user details object, expected class [%s]"
              .formatted(AuthorizedUserDetails.class.getName()));
    }

    return details;
  }
}
