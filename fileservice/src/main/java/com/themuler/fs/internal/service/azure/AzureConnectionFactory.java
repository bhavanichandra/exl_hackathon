package com.themuler.fs.internal.service.azure;

import com.themuler.fs.internal.model.AuthenticationConfiguration;
import com.themuler.fs.internal.model.AuthenticationData;
import com.themuler.fs.internal.service.auth.AccessInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AzureConnectionFactory {
  private final AccessInterface credentialsService;

  @Value("${environment.active}")
  private String env;

  public Map<String, Object> getAzureCredentials() {
    AuthenticationData credentials = this.credentialsService.getClientConfiguration(env);
    return credentials.getConfigurations().stream()
        .filter(each -> each.getCloudName().equals("azure"))
        .map(AuthenticationConfiguration::getCredentials)
        .collect(Collectors.toList())
        .get(0);
  }
}
