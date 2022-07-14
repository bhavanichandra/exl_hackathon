package com.themuler.fs.internal.service.aws;

import com.themuler.fs.internal.model.ClientConfig;
import com.themuler.fs.internal.repository.ClientConfigRepository;
import com.themuler.fs.internal.repository.CloudPlatformRepository;
import com.themuler.fs.internal.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Log4j2
public class AwsConnectionFactory implements AwsCredentialsProvider {

  private final CredentialsService credentialsService;

  private Map<String,Object> credential;

  @Value("${environment.active}")
  private String environment;

  @Override
  public AwsCredentials resolveCredentials() {
    List<Map<String, Object>> credentialsFromConfiguration =
        this.credentialsService.getCredentialsFromConfiguration(environment);
    Map<String, Object> credential =
        credentialsFromConfiguration.stream()
            .filter(each -> each.get("cloud").equals("aws"))
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
