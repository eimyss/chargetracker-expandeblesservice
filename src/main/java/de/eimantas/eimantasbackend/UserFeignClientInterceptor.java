package de.eimantas.eimantasbackend;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserFeignClientInterceptor implements RequestInterceptor {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_TOKEN_TYPE = "Bearer";

  @Override
  public void apply(RequestTemplate template) {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();

    if (authentication != null && authentication instanceof KeycloakAuthenticationToken) {
      KeycloakAuthenticationToken details = (KeycloakAuthenticationToken) authentication;
      template.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, ((KeycloakAuthenticationToken) authentication).getAccount().getKeycloakSecurityContext().getTokenString()));
    }
  }
}