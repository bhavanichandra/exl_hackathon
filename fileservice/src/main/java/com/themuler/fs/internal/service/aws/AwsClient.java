package com.themuler.fs.internal.service.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.Map;

@Component
public class AwsClient implements AutoCloseable {

  private final S3Client s3Client;

  private final S3Presigner s3Presigner;

  private Map<String, Object> credentials;

  @Autowired
  public AwsClient(AwsConnectionFactory factory) {
    this.s3Client =
        S3Client.builder().credentialsProvider(factory).region(Region.AP_SOUTH_1).build();
    this.s3Presigner =
        S3Presigner.builder().credentialsProvider(factory).region(Region.AP_SOUTH_1).build();
    this.credentials = factory.getCredential();
  }

  public S3Client getS3Client() {
    return s3Client;
  }

  public S3Presigner getS3Presigner() {
    return s3Presigner;
  }

  @Override
  public void close() throws Exception {
    this.s3Client.close();
  }

  public Map<String, Object> getCredentials() {
    return credentials;
  }
}
