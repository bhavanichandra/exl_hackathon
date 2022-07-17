package com.themuler.fs.internal.service.auth;

import com.themuler.fs.api.Feature;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.AuthenticationConfiguration;
import com.themuler.fs.internal.model.AuthenticationData;
import com.themuler.fs.internal.model.ClientConfiguration;
import com.themuler.fs.internal.service.utility.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Log4j2
@RequiredArgsConstructor
public class AuthenticationHandler implements AccessInterface {

  private final EncryptionUtils encryptionUtils;

  @Override
  public Boolean allowAccess(Feature feature) {
    AppUser user = getPrincipal();
    if (user == null) {
      return false;
    }
    return user.getRole().allowedFeatures().contains(feature);
  }

  @Override
  public AuthenticationData getClientConfiguration(String environment) {
    AppUser user = getPrincipal();
    if (user == null) {
      return null;
    }
    List<AuthenticationConfiguration> configurations =
        user.getClient().getClientConfigurations().stream()
            .filter(each -> each.getEnvironment().equalsIgnoreCase(environment))
            .map(
                each -> {
                  Map<String, String> decrypted = decryptCredentials(each);
                  return AuthenticationConfiguration.builder()
                      .cloudName(user.getClient().getCloudPlatform().getCloudPlatform())
                      .credentials(decrypted)
                      .encryptedKeys(each.getEncryptedFields())
                      .build();
                })
            .collect(Collectors.toList());
    return AuthenticationData.builder()
        .userId(user.getId())
        .clientId(user.getClient().getId())
        .configurations(configurations)
        .build();
  }

  private Map<String, String> decryptCredentials(ClientConfiguration clientConfiguration) {
    Map<String, String> credentials = new HashMap<>();
    String encryptedKeys = clientConfiguration.getEncryptedFields();
    clientConfiguration
        .getCredentials()
        .forEach(
            (key, value) -> {
              if (encryptedKeys == null) {
                credentials.put(key, value);
              } else if (encryptedKeys.equals("all")) {
                credentials.put(key, encryptionUtils.decrypt(value));
              } else {
                List<String> encryptedFieldList =
                    Arrays.stream(encryptedKeys.split(",")).collect(Collectors.toList());
                if (encryptedFieldList.contains(key)) {
                  credentials.put(key, encryptionUtils.decrypt(value));
                } else {
                  credentials.put(key, value);
                }
              }
            });
    return credentials;
  }

  private AppUser getPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }
    return (AppUser) authentication.getPrincipal();
  }
}
