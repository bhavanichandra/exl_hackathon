package com.themuler.fs.internal.service;

import com.themuler.fs.internal.model.ClientConfig;
import com.themuler.fs.internal.repository.ClientConfigRepository;
import com.themuler.fs.internal.service.auth.AppUserDetailsService;
import com.themuler.fs.internal.service.auth.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CredentialsService {

  private final ClientConfigRepository clientConfigRepository;

  private final AuthenticationHandler authenticationHandler;

  public List<Map<String, Object>> getCredentialsFromConfiguration(String environment) {
    var authentication = authenticationHandler.getAuthentication();
    var userDetails = (AppUserDetailsService.AppUserDetails) authentication.getPrincipal();
    var client = userDetails.getUser().getClient();
    Iterable<ClientConfig> configList = clientConfigRepository.findAllByClient(client);
    List<ClientConfig> configurations = new ArrayList<>();
    configList.forEach(configurations::add);
    return configurations.stream()
        .filter(each -> each.getEnvironment().equals(environment))
        .map(
            each -> {
              var credential = each.getCredential();
              credential.put("cloud", each.getCloudPlatform().getName());
              return credential;
            })
        .collect(Collectors.toList());
  }
}
