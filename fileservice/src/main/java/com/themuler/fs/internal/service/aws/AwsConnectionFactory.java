package com.themuler.fs.internal.service.aws;

import com.themuler.fs.api.CloudPlatform;
import com.themuler.fs.internal.exception.EmptyCredentialsException;
import com.themuler.fs.internal.model.AuthenticationConfiguration;
import com.themuler.fs.internal.model.AuthenticationData;
import com.themuler.fs.internal.service.auth.AccessInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class AwsConnectionFactory implements AwsCredentialsProvider {

  private final AccessInterface credentialsService;

  private Map<String, String> credentials;

  @Value("${environment.active}")
  private String environment;

  @Override
  public AwsCredentials resolveCredentials() {
    try {
      AuthenticationData credentialsFromConfiguration =
          this.credentialsService.getClientConfiguration(environment);
      Optional<AuthenticationConfiguration> authConfig =
          credentialsFromConfiguration.getConfigurations().stream()
              .filter(each -> each.getCloudName().equals(CloudPlatform.AWS.getCloudPlatform()))
              .findFirst();
      if (authConfig.isEmpty()) {
        throw new EmptyCredentialsException("Credentials are empty. Please configure them!");
      }
      AuthenticationConfiguration configuration = authConfig.get();
      this.credentials = configuration.getCredentials();
      return AwsBasicCredentials.create(
          credentials.get("client_id"), credentials.get("client_secret"));
    } catch (Exception ex) {
      log.error("Error retrieving credentials: {} ", ex.getMessage());
      return AwsBasicCredentials.create(null, null);
    }
  }

  public Map<String, String> getCredential() {
    return credentials;
  }
}
