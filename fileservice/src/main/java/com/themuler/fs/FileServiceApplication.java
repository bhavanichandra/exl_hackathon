package com.themuler.fs;

import com.themuler.fs.service.AwsConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.stream.ByteStreamReadingMessageSource;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

@SpringBootApplication
@RefreshScope
@EnableWebFlux
@EnableIntegration
public class FileServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FileServiceApplication.class, args);
  }
}

@Component
class AwsClient implements AutoCloseable {

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

@EnableIntegrationGraphController
@RequiredArgsConstructor
class Uploader {

  private final AwsClient awsClient;

  @Bean
  public MessageChannel inputChannel(){
    return MessageChannels.direct().get();
  }
  @Bean
  public MessageChannel requestChannel() {
    return MessageChannels.flux().get();
  }


  @Bean
  public HttpRequestHandlingMessagingGateway httpGateway() {
    HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway(true);
    RequestMapping mapping = new RequestMapping();
    mapping.setMethods(HttpMethod.POST);
    mapping.setPathPatterns("/test");
    gateway.setRequestMapping(mapping);
    gateway.setRequestChannel(requestChannel());
    gateway.setRequestPayloadTypeClass(byte[].class);
    return gateway;
  }

  @Bean
  public IntegrationFlow httpInboundFlow() {
    return IntegrationFlows.from(
            WebFlux.inboundChannelAdapter("/load")
                .requestMapping(req -> req.methods(HttpMethod.GET, HttpMethod.POST))
                .requestPayloadType(ResolvableType.forClassWithGenerics(Flux.class, FilePart.class))
                .requestChannel("streamingChannel"))
        .get();
  }

  @Bean
  public IntegrationFlow streamListenerFlow() {
    return IntegrationFlows.from("streamingChannel")
        .transform(
            m -> {
              var s3 = awsClient.getS3Client();
              var bucketName = "exl-hackthon";
              var message = (Map<String, Object>) m;
              String fileName = (String) message.get("fileName");
              long contentLength = (Long) message.get("size");
              String contentType = (String) message.get("contentType");
              InputStream inputStream = (FileInputStream) message.get("fileContent");
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
                return "Failed";
              }
              System.out.println(putObjectResponse);
              return ResponseEntity.ok(putObjectResponse);
            })
        .get();
  }
}
