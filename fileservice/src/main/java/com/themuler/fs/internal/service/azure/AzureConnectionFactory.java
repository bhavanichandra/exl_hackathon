package com.themuler.fs.internal.service.azure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuler.fs.internal.model.ClientConfig;
import com.themuler.fs.internal.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AzureConnectionFactory {
  public static final String AZURE_TOKEN_URL = "https://login.microsoftonline.com";
  private final RestTemplate restTemplate;
  private final CredentialsService credentialsService;

  private final ObjectMapper mapper;

  private Map<String, Object> credential;

  @Value("${environment}")
  private String env;

  public Map<String, Object> getAzureToken() {
    List<Map<String, Object>> credentials =
        this.credentialsService.getCredentialsFromConfiguration(env);
    Map<String, Object> credential =
        credentials.stream()
            .filter(each -> each.get("cloud").equals("azure"))
            .collect(Collectors.toList())
            .get(0);
    this.credential = credential;
    String tenantId = (String) credential.get("tenant_id");
    var uri =
        UriComponentsBuilder.fromUri(URI.create(AZURE_TOKEN_URL))
            .path(tenantId)
            .path("oauth2/v2.0/token")
            .build(true)
            .toUri();
    credential.remove("default_bucket_name");
    credential.remove("storage_account");
    credential.remove("tenant_id");
    RequestEntity<Map<String, Object>> body =
        RequestEntity.post(uri).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(credential);
    ResponseEntity<String> exchange = this.restTemplate.exchange(body, String.class);
    String response = exchange.getBody();
    return mapper.convertValue(response, new TypeReference<Map<String, Object>>() {});
  }

  public Map<String, Object> getCredential() {
    return credential;
  }
}
