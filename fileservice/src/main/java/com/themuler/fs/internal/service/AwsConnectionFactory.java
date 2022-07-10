package com.themuler.fs.internal.service;

import com.themuler.fs.internal.repository.ClientConfigRepository;
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
    return AwsBasicCredentials.create(
        credentialsMap.get("aws_client_id"), credentialsMap.get("aws_client_secret"));
  }
}
