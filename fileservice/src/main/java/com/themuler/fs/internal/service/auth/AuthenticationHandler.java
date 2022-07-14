package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import com.themuler.fs.api.UserRole;
import com.themuler.fs.internal.model.AuthenticationData;
import com.themuler.fs.internal.model.Client;
import com.themuler.fs.internal.model.ClientConfig;
import com.themuler.fs.internal.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthenticationHandler implements AccessInterface {

  @Value("${environment.active}")
  private String environment;

  @Override
  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Override
  public boolean allowAccess(Feature feature) {
    Authentication authentication = this.getAuthentication();
    var principal = (AppUserDetailsService.AppUserDetails) authentication.getPrincipal();
    String currentUserRole = principal.currentUserRole();
    UserRole userRole = UserRole.valueOf(currentUserRole);
    return userRole.allowedFeatures().contains(feature);
  }

  @Override
  public AuthenticationData authenticationInformation() {
    Authentication authentication = this.getAuthentication();
    var principal = (AppUserDetailsService.AppUserDetails) authentication.getPrincipal();
    User user = principal.getUser();
    Client client = user.getClient();
    List<AuthenticationData.Configuration> configs =
        client.getClientConfigs().stream()
            .filter(each -> each.getEnvironment().equals(environment))
            .map(
                each -> {
                  Map<String, Object> credential = each.getCredential();
                  String cloudName = each.getCloudPlatform().getName();
                  return AuthenticationData.Configuration.builder()
                      .cloudName(cloudName)
                      .credentials(credential)
                      .build();
                })
            .collect(Collectors.toList());

    return AuthenticationData.builder()
        .clientId(client.getId().toString())
        .userId(user.getId().toString())
        .configurations(configs)
        .build();
  }
}
