package com.themuler.fs.internal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuler.fs.api.CloudPlatform;
import com.themuler.fs.api.OperationConstants;
import com.themuler.fs.internal.service.IntegrationServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
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
            .log(
                    LoggingHandler.Level.INFO,
                    "com.themuler.fs.internal.config.FileServiceIntegrationConfig",
                    "headers.operation")
        .route(
            "headers.operation",
            operationRouter ->
                operationRouter
                    .channelMapping(OperationConstants.UPLOAD, "uploadChannel")
                    .channelMapping(OperationConstants.DOWNLOAD, "downloadChannel")
                    .channelMapping(OperationConstants.TEMP_DOWNLOAD, "tempDownloadChannel"))
        .get();
  }

  @Bean
  public IntegrationFlow uploadFlow() {
    return IntegrationFlows.from(this.uploadChannel())
        .log(
            LoggingHandler.Level.INFO,
            "com.themuler.fs.internal.config.FileServiceIntegrationConfig",
            "payload")
        .route(
            "headers.cloud",
            cloudRouter ->
                cloudRouter
                    .subFlowMapping(
                        CloudPlatform.AWS,
                        awsFlow -> awsFlow.handle(integrationServiceInterface, "uploadToAws"))
                    .subFlowMapping(
                        CloudPlatform.AZURE,
                        azureFlow -> azureFlow.handle(integrationServiceInterface, "uploadToAzure"))
                    .subFlowMapping(
                        CloudPlatform.GOOGLE_CLOUD_PLATFORM,
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
                        CloudPlatform.AWS,
                        awsFlow -> awsFlow.handle(integrationServiceInterface, "downloadFromAws"))
                    .subFlowMapping(
                        CloudPlatform.AZURE,
                        azureFlow ->
                            azureFlow.handle(integrationServiceInterface, "downloadFromAzure"))
                    .subFlowMapping(
                        CloudPlatform.GOOGLE_CLOUD_PLATFORM,
                        gcpFlow -> gcpFlow.handle(integrationServiceInterface, "downloadFromGcp")))
        .get();
  }
}
