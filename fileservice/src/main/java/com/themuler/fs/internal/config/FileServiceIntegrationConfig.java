package com.themuler.fs.internal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuler.fs.internal.service.IntegrationServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class FileServiceIntegrationConfig {

  private final IntegrationServiceInterface integrationServiceInterface;

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
                    .channelMapping("upload", "uploadChannel")
                    .channelMapping("download", "downloadChannel"))
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
                cloudRouter
                    .subFlowMapping(
                        "aws",
                        awsFlow -> awsFlow.log().handle(integrationServiceInterface, "uploadToAws"))
                    .subFlowMapping(
                        "azure",
                        azureFlow -> azureFlow.handle(integrationServiceInterface, "uploadToAzure"))
                    .subFlowMapping(
                        "gcp",
                        gcpFlow -> gcpFlow.handle(integrationServiceInterface, "uploadToGcp")))
        .get();
  }

  @Bean
  public IntegrationFlow downloadFlow() {
    return IntegrationFlows.from("downloadChannel")
        .handle(integrationServiceInterface, "getDownloadLocation")
        .log()
        .route(
            "payload",
            router ->
                router
                    .subFlowMapping(
                        "aws",
                        awsFlow -> awsFlow.handle(integrationServiceInterface, "downloadFromAws"))
                    .subFlowMapping(
                        "azure",
                        azureFlow ->
                            azureFlow.handle(integrationServiceInterface, "downloadFromAzure"))
                    .subFlowMapping(
                        "gcp",
                        gcpFlow -> gcpFlow.handle(integrationServiceInterface, "downloadFromGcp")))
        .get();
  }
}
