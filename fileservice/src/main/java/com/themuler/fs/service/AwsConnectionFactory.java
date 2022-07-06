package com.themuler.fs.service;

import com.themuler.fs.repository.ClientConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Log4j2
public class AwsConnectionFactory implements AwsCredentialsProvider {

  private final ClientConfigRepository clientConfigRepository;

  @Override
  public AwsCredentials resolveCredentials() {
    var credentialsMap = new HashMap<String, String>();
    this.clientConfigRepository
        .findAll()
        .forEach(
            config -> credentialsMap.put(config.getProperty_name(), config.getProperty_value()));
    return AwsBasicCredentials.create(
        credentialsMap.get("aws_client_id"), credentialsMap.get("aws_client_secret"));
  }
}
