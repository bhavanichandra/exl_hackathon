package com.themuler.fs.internal.service.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Component
public class AwsClient implements AutoCloseable {

  private final S3Client s3Client;

  private final S3Presigner s3Presigner;

  @Autowired
  public AwsClient(AwsConnectionFactory factory) {
    this.s3Client =
        S3Client.builder().credentialsProvider(factory).region(Region.AP_SOUTH_1).build();
    this.s3Presigner =
        S3Presigner.builder().credentialsProvider(factory).region(Region.AP_SOUTH_1).build();
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
}
