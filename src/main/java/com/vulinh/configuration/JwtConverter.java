package com.vulinh.configuration;

import com.vulinh.exception.AuthorizationException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

  static final String RESOURCE_ACCESS_CLAIM = "resource_access";
  static final String EMAIL_CLAIM = "email";

  private final ApplicationProperties applicationProperties;

  @Override
  @SuppressWarnings("unchecked")
  public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
    var clientName = applicationProperties.clientName();

    // cannot have different authorized party
    if (!clientName.equalsIgnoreCase(jwt.getClaimAsString("azp"))) {
      throw new AuthorizationException(
          "Invalid authorized party (azp), expected [%s]".formatted(clientName));
    }

    // get the top-level "resource_access" claim.
    var resourceAccess =
        nonMissing(jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM), RESOURCE_ACCESS_CLAIM);

    // get the map specific to our client ID.
    var clientRolesMap =
        (Map<String, Collection<String>>)
            getMapValue(resourceAccess, clientName, RESOURCE_ACCESS_CLAIM);

    // get the collection of role strings from that map.
    var roleNames = getMapValue(clientRolesMap, "roles", RESOURCE_ACCESS_CLAIM, clientName);

    var authorities =
        roleNames.stream()
            .filter(StringUtils::isNotBlank)
            .map(String::toUpperCase)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

    var userDetails =
        new SecurityConfig.AuthorizedUserDetails(
            UUID.fromString(nonMissing(jwt.getSubject(), "subject")),
            nonMissing(jwt.getClaimAsString("preferred_username"), "username"),
            nonMissing(jwt.getClaimAsString(EMAIL_CLAIM), EMAIL_CLAIM),
            authorities);

    return UsernamePasswordAuthenticationToken.authenticated(
        userDetails, jwt.getTokenValue(), authorities);
  }

  private static <T> T getMapValue(Map<String, T> map, String key, String... origin) {
    var claimName =
        ArrayUtils.isEmpty(origin) ? key : "%s.%s".formatted(String.join(".", origin), key);

    return nonMissing(map.get(key), claimName);
  }

  private static <T> T nonMissing(T object, String name) {
    if (object == null) {
      throw new AuthorizationException("Claim [%s] is missing".formatted(name));
    }

    return object;
  }
}
