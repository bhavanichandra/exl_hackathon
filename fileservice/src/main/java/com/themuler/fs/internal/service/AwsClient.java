package com.themuler.fs.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class AwsClient implements AutoCloseable {

  private final S3Client s3Client;

  @Autowired
  public AwsClient(AwsConnectionFactory factory) {
    this.s3Client =
            S3Client.builder().credentialsProvider(factory).region(Region.AP_SOUTH_1).build();
  }

  public S3Client getS3Client() {
    return s3Client;
  }

  @Override
  public void close() throws Exception {
    this.s3Client.close();
  }
}