package com.themuler.fs;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.stream.ByteStreamReadingMessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;

@MessagingGateway(defaultRequestChannel = "testStreamChannel")
interface TestGateway {
  void process(Object message);
}

interface TestServiceInterface {
  void test(Object msg);
}

@SpringBootApplication
@RefreshScope
public class FileServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(FileServiceApplication.class, args);
  }
}

@Component
@RequiredArgsConstructor
class Initializer implements CommandLineRunner {

  private final TestGateway gateway;

  @Override
  public void run(String... args) throws Exception {
    try (FileInputStream stream = new FileInputStream(args[0])) {
      this.gateway.process(stream);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

@Configuration
class Configure {

  @Bean
  public MessageChannel testStreamChannel() {
    return MessageChannels.flux().get();
  }

  public ByteStreamReadingMessageSource messageSource(InputStream stream) {
    return new ByteStreamReadingMessageSource(stream);
  }


  @ServiceActivator
  public String testHandler(Object object) {
    System.out.println(object);
    return "Done";
  }

  @Bean
  public IntegrationFlow testFlow() {
    return IntegrationFlows.from(TestServiceInterface.class, f -> f.beanName("testBean2"))
        .log()
        .transform(this::testHandler)
        .get();
  }
}
