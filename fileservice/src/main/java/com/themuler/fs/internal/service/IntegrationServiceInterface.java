package com.themuler.fs.internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.themuler.fs.api.CloudPlatform;
import com.themuler.fs.api.DownloadAPIRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.util.Map;

public interface IntegrationServiceInterface {
  Object uploadToAws(Message<Map<String, Object>> message);

  Message<CloudPlatform> getDownloadLocation(Message<DownloadAPIRequest> request);
  ResponseEntity<?>  downloadFromAws(Message<String> message);

  ResponseEntity<?>  downloadFromAzure(Message<?> message);

  ResponseEntity<?>  downloadFromGcp(Message<?> message) throws IOException;


  Object uploadToAzure(Message<Map<String, Object>> message) throws IOException;

  Object uploadToGcp(Message<Map<String, Object>> message) throws IOException;

  ResponseEntity<?> temporaryDownload(Message<DownloadAPIRequest> message) throws IOException;
}
