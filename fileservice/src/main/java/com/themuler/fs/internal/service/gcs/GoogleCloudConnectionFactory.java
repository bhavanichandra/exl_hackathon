package com.themuler.fs.internal.service.gcs;

import com.themuler.fs.internal.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoogleCloudConnectionFactory {
  private final CredentialsService credentialsService;

  @Value("${environment}")
  private String env;

  public Map<String, Object> getGCSCredentials() {
    List<Map<String, Object>> credentials =
        this.credentialsService.getCredentialsFromConfiguration(env);
    return credentials.stream()
        .filter(each -> each.get("cloud").equals("gcs"))
        .collect(Collectors.toList())
        .get(0);
  }
}
