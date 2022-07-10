package com.themuler.fs.internal.service;

import com.themuler.fs.api.DownloadAPIRequest;
import org.springframework.messaging.Message;

import java.util.Map;

public interface IntegrationServiceInterface {
  Object uploadToAws(Message<Map<String, Object>> message);

  Message<String> getDownloadLocation(DownloadAPIRequest request);
  Object downloadFromAws(Message<String> message);

  Object getToken();
}
