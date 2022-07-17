package com.themuler.fs.internal.service.aws;

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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class AwsConnectionFactory implements AwsCredentialsProvider {

  private final AccessInterface credentialsService;

  private Map<String, Object> credential;

  @Value("${environment.active}")
  private String environment;

  @Override
  public AwsCredentials resolveCredentials() {
    AuthenticationData credentialsFromConfiguration =
        this.credentialsService.getClientConfiguration(environment);
    Map<String, Object> credential =
        credentialsFromConfiguration.getConfigurations().stream()
            .filter(each -> each.getCloudName().equals("aws"))
            .map(AuthenticationConfiguration::getCredentials)
            .collect(Collectors.toList())
            .get(0);
    this.credential = credential;
    return AwsBasicCredentials.create(
        (String) credential.get("client_id"), (String) credential.get("client_secret"));
  }

  public Map<String, Object> getCredential() {
    return credential;
  }
}
