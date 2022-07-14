package com.themuler.fs.internal.gateway;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;

@MessagingGateway
public interface FileServiceMessageGateway {

  @Gateway(requestChannel = "inputChannel")
  Object send(Message<?> message);

}
