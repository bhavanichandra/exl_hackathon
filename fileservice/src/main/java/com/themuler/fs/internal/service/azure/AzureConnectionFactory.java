package com.themuler.fs.internal.service.azure;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuler.fs.internal.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AzureConnectionFactory {
  private final CredentialsService credentialsService;

  @Value("${environment}")
  private String env;

  public Map<String, Object> getAzureCredentials() {
    List<Map<String, Object>> credentials =
        this.credentialsService.getCredentialsFromConfiguration(env);
    Map<String, Object> credential =
        credentials.stream()
            .filter(each -> each.get("cloud").equals("azure"))
            .collect(Collectors.toList())
            .get(0);
    return credential;
  }
}
