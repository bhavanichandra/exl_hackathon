package com.themuler.fs.internal.service.gcs;

import com.themuler.fs.api.CloudPlatform;
import com.themuler.fs.internal.model.AuthenticationConfiguration;
import com.themuler.fs.internal.model.AuthenticationData;
import com.themuler.fs.internal.service.auth.AccessInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleCloudConnectionFactory {
  private final AccessInterface credentialsService;

  @Value("${environment.active}")
  private String env;

  public Map<String, String> getGCSCredentials() {
    AuthenticationData credentials = this.credentialsService.getClientConfiguration(env);
    Optional<AuthenticationConfiguration> authConfig =
        credentials.getConfigurations().stream()
            .filter(
                each ->
                    each.getCloudName()
                        .equals(CloudPlatform.GOOGLE_CLOUD_PLATFORM.getCloudPlatform()))
            .findFirst();
    if(authConfig.isEmpty()) {
      return null;
    }
    return authConfig.get().getCredentials();
  }
}
