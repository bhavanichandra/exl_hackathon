package com.themuler.fs.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;

//@MessagingGateway(defaultRequestChannel = "inputChannel")
public interface FileServiceMessageGateway {

  Object send(Message<?> message);

}
