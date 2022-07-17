package com.themuler.fs.internal.service.azure;

import com.themuler.fs.api.CloudPlatform;
import com.themuler.fs.internal.model.AuthenticationConfiguration;
import com.themuler.fs.internal.model.AuthenticationData;
import com.themuler.fs.internal.service.auth.AccessInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AzureConnectionFactory {
  private final AccessInterface credentialsService;

  @Value("${environment.active}")
  private String env;

  public Map<String, String> getAzureCredentials() {
    AuthenticationData credentials = this.credentialsService.getClientConfiguration(env);
    Optional<AuthenticationConfiguration> authConfig =
            credentials.getConfigurations().stream()
                    .filter(
                            each ->
                                    each.getCloudName()
                                            .equals(CloudPlatform.AZURE.getCloudPlatform()))
                    .findFirst();
    if(authConfig.isEmpty()) {
      return null;
    }
    return authConfig.get().getCredentials();
  }
}
