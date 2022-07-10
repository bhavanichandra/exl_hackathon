package com.themuler.fs.internal.service;

import com.themuler.fs.internal.model.ClientConfig;
import com.themuler.fs.internal.repository.ClientConfigRepository;
import com.themuler.fs.internal.repository.CloudPlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Log4j2
public class AwsConnectionFactory implements AwsCredentialsProvider {

  private final ClientConfigRepository clientConfigRepository;

  private final CloudPlatformRepository cloudPlatformRepository;

  private final AuthenticationHandler authenticationHandler;

  @Override
  public AwsCredentials resolveCredentials() {
    var authentication = authenticationHandler.getAuthentication();
    var userDetails = (AppUserDetailsService.AppUserDetails) authentication.getPrincipal();
    var client = userDetails.getUser().getClient();
    var aws = cloudPlatformRepository.findByName("aws").orElse(null);
    Iterable<ClientConfig> configList =
        clientConfigRepository.findByClientAndCloudPlatform(client, aws);
    Optional<ClientConfig> devConfig =
        StreamSupport.stream(configList.spliterator(), false)
            .filter(each -> each.getEnvironment().equals("dev"))
            .findFirst();
    ClientConfig config = devConfig.get();
    Map<String, Object> credential = config.getCredential();
    return AwsBasicCredentials.create(
        (String) credential.get("client_id"), (String) credential.get("client_secret"));
  }
}
