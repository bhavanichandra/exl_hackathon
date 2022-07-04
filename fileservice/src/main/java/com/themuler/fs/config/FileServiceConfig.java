package com.themuler.fs.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@Configuration
@RequiredArgsConstructor
public class FileServiceConfig {

  private final AmazonS3 amazonS3;


  @Bean
  public MessageChannel healthCheck() {
    return MessageChannels.direct().get();
  }

  @Bean
  public MessageChannel inputChannel() {
    return MessageChannels.flux().get();
  }

  @Bean
  public MessageChannel uploadChannel() {
    return MessageChannels.flux().get();
  }

  @Bean
  public IntegrationFlow routerFlow() {
    return IntegrationFlows.from(this.inputChannel())
        .<Message<?>, String>route(
            msg -> (String) msg.getHeaders().get("operation"),
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
        .<Message<?>, String>route(
            msg -> (String) msg.getHeaders().get("cloud"),
            cloudRouter -> cloudRouter.subFlowMapping("aws", awsFlow -> awsFlow.transform(m -> m)))
        .get();
  }
}
