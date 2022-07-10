package com.themuler.fs.internal.service.gcs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleCloudConnectionFactory {
  public static final String GCS_TOKEN_URL = "";
  private static final String GCS_API_URL = "";
  private final RestTemplate restTemplate;
}
