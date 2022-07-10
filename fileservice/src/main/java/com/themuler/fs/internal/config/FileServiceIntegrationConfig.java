package com.themuler.fs.internal.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.service.IntegrationServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class FileServiceIntegrationConfig {

  private final IntegrationServiceInterface integrationServiceInterface;

  @Value("${azure.token.url}")
  private String azureTokenUrl;

  @Value("${azure.api.base.url}")
  private String azureBaseUrl;

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
                        azureFlow ->
                            azureFlow
                                .enrich(
                                    enricherSpec ->
                                        enricherSpec
                                            .requestChannel("credentials.input")
                                            .propertyExpression("creds", "payload")
                                            .propertyExpression("headers", "headers"))
                                .handle(
                                    Http.outboundGateway(
                                        m ->
                                            UriComponentsBuilder.fromUriString(
                                                    this.azureTokenUrl
                                                        + "/"
                                                        + m.getHeaders()
                                                            .get("headers", Map.class)
                                                            .get("storage_account")
                                                        + "/oauth2/v2.0/token")
                                                .toUriString()))
                                .log())
                    .subFlowMapping("gcp", gcpFlow -> gcpFlow.log()))
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
                    .subFlowMapping("aws", awsFlow -> awsFlow.log())
                    .subFlowMapping("azure", azureFlow -> azureFlow.log())
                    .subFlowMapping("gcp", gcp -> gcp.log()))
        .get();
  }

  @Bean
  public IntegrationFlow credentials() {
    return IntegrationFlows.from("credentials.input")
        .handle(integrationServiceInterface, "azureCredentials")
        .transform(
            m -> {
              var responseWrapper = objectMapper().convertValue(m, ResponseWrapper.class);
              Map<String, Object> azureCredentials =
                  objectMapper()
                      .convertValue(responseWrapper.getPayload(), new TypeReference<>() {});
              String storage_account = (String) azureCredentials.get("storage_account");
              String default_bucket_name = (String) azureCredentials.get("default_bucket_name");
              String tenant_id = (String) azureCredentials.get("tenant_id");
              String cloud = (String) azureCredentials.get("cloud");
              azureCredentials.remove("storage_account");
              azureCredentials.remove("default_bucket_name");
              azureCredentials.remove("tenant_id");
              azureCredentials.remove("cloud");
              return MessageBuilder.withPayload(azureCredentials)
                  .setHeader("storage_account", storage_account)
                  .setHeader("default_bucket_name", default_bucket_name)
                  .setHeader("cloud", cloud)
                  .setHeader("tenant_id", tenant_id)
                  .build();
            })
        .handle(
            Http.outboundGateway(
                    h ->
                        UriComponentsBuilder.fromUriString(
                                this.azureTokenUrl
                                    + "/"
                                    + h.getHeaders().get("tenant_id", String.class)
                                    + "/oauth2/v2.0/token")
                            .toUriString())
                .httpMethod(HttpMethod.POST)
                .expectedResponseType(String.class)
                .extractResponseBody(true)
                .get())
        .get();
  }
}
