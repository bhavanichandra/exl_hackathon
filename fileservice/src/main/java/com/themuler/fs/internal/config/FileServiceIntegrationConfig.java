package com.themuler.fs.internal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuler.fs.internal.model.VirtualFileSystem;
import com.themuler.fs.internal.repository.CloudPlatformRepository;
import com.themuler.fs.internal.repository.VFSRepository;
import com.themuler.fs.internal.service.AwsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class FileServiceIntegrationConfig {

  private final AwsClient awsClient;

  private final VFSRepository vfsRepository;
  private final CloudPlatformRepository cloudPlatformRepository;

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }



  @Bean
  public MessageChannel healthCheck() {
    return MessageChannels.direct().get();
  }

  @Bean
  public MessageChannel inputChannel() {
    return MessageChannels.direct().get();
  }

  @Bean
  public MessageChannel uploadChannel() {
    return MessageChannels.direct().get();
  }

  @Bean
  public IntegrationFlow routerFlow() {
    return IntegrationFlows.from(this.inputChannel())
        .log()
        .route(
            "headers.operation",
            operationRouter ->
                operationRouter
                    .channelMapping("health", "healthCheck")
                    .channelMapping("upload", "uploadChannel"))
        .get();
  }

  @Bean
  public IntegrationFlow healthCheckFlow() {
    return IntegrationFlows.from(this.healthCheck()).transform(m -> "Working Fine").get();
  }

  @Bean
  public IntegrationFlow uploadFlow() {
    return IntegrationFlows.from(this.uploadChannel())
        .route(
            "headers.cloud",
            cloudRouter ->
                cloudRouter.subFlowMapping(
                    "aws",
                    awsFlow ->
                        awsFlow.transform(
                            m -> {
                              var s3 = awsClient.getS3Client();
                              var bucketName = "exl-hackthon";
                              var message = (Map<String, Object>) m;
                              String fileName = (String) message.get("fileName");
                              long contentLength = (Long) message.get("size");
                              String contentType = (String) message.get("contentType");
                              InputStream inputStream =
                                  (FileInputStream) message.get("fileContent");
                              var key = "data/" + fileName;
                              PutObjectResponse putObjectResponse =
                                  s3
                                      .putObject(
                                          PutObjectRequest.builder()
                                              .bucket(bucketName)
                                              .key(key)
                                              .contentLength(contentLength)
                                              .contentType(contentType)
                                              .build(),
                                          RequestBody.fromInputStream(inputStream, contentLength))
                                      .toBuilder()
                                      .build();
                              if (putObjectResponse == null) {
                                return ResponseEntity.status(500).body("Failed");
                              }
                              System.out.println(putObjectResponse);
                              VirtualFileSystem vfs = new VirtualFileSystem();
                              vfs.setCloud_unique_identifier(key);
//                              vfs.setCloud_location(putObjectResponse.);
                              var aws = cloudPlatformRepository.findByName("aws").get();
                              vfs.setCloudPlatform(aws);
                              vfs.setType("File");
                              vfs.setParent(null);
                              VirtualFileSystem savedVFS = vfsRepository.save(vfs);
                              return ResponseEntity.ok(savedVFS);
                            })))
        .get();
  }
}
