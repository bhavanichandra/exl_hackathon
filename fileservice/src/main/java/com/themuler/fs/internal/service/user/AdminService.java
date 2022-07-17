package com.themuler.fs.internal.service.user;

import com.themuler.fs.api.CloudPlatform;
import com.themuler.fs.api.NewClient;
import com.themuler.fs.api.NewClientConfiguration;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.Client;
import com.themuler.fs.internal.model.ClientConfiguration;
import com.themuler.fs.internal.repository.ClientConfigurationRepository;
import com.themuler.fs.internal.repository.ClientRepository;
import com.themuler.fs.internal.service.utility.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AdminService implements AdminInterface {

  private final ClientRepository clientRepository;

  private final ClientConfigurationRepository clientConfigurationRepository;

  private final EncryptionUtils encryptionUtils;

  @Override
  public ResponseWrapper<Client> addClient(NewClient client) {
    ResponseWrapper.ResponseWrapperBuilder<Client> builder = ResponseWrapper.builder();
    try {
      Optional<CloudPlatform> cloudPlatformOptional =
          Arrays.stream(CloudPlatform.values())
              .filter(each -> each.getCloudPlatform().equalsIgnoreCase(client.getCloudPlatform()))
              .findFirst();
      if (cloudPlatformOptional.isEmpty()) {
        return builder
            .message("Allowed values for cloudPlatform are: " + CloudPlatform.toValues())
            .success(false)
            .payload(null)
            .build();
      }
      CloudPlatform cp = cloudPlatformOptional.get();
      Client newClient =
          Client.builder()
              .name(client.getName())
              .cloudPlatform(cp)
              .clientConfigurations(new ArrayList<>())
              .build();
      Client savedClient = this.clientRepository.save(newClient);
      return builder.message("New Client Added!").success(true).payload(savedClient).build();

    } catch (Exception ex) {
      return builder
          .message("Unexpected Error: " + ex.getMessage())
          .success(false)
          .payload(null)
          .build();
    }
  }

  @Override
  public ResponseWrapper<Client> addClientConfiguration(
      String clientId, NewClientConfiguration clientConfiguration) {
    ResponseWrapper.ResponseWrapperBuilder<Client> builder = ResponseWrapper.builder();
    Optional<Client> clientOpt = this.clientRepository.findById(new ObjectId(clientId));
    if (clientOpt.isEmpty()) {
      return builder.message("No Client with id exists").success(false).payload(null).build();
    }
    Map<String, String> credentials;
    if (clientConfiguration.getPerformEncryption()) {

      String fieldsToEncrypt = clientConfiguration.getFieldsToEncrypt();
      if (fieldsToEncrypt.equalsIgnoreCase("all")) {
        Map<String, String> encryptedCredentials = new HashMap<>();
        clientConfiguration
            .getCredentials()
            .forEach((k, v) -> encryptedCredentials.put(k, encryptionUtils.encrypt(v)));
        credentials = encryptedCredentials;
      } else {
        Map<String, String> encryptedCredentials = new HashMap<>();
        List<String> encryptedFields =
            Arrays.stream(clientConfiguration.getFieldsToEncrypt().split(","))
                .collect(Collectors.toList());
        clientConfiguration
            .getCredentials()
            .forEach(
                (k, v) -> {
                  if (encryptedFields.contains(k)) {
                    encryptedCredentials.put(k, encryptionUtils.encrypt(v));
                  } else {
                    encryptedCredentials.put(k, v);
                  }
                });
        credentials = encryptedCredentials;
      }
    } else {
      credentials = clientConfiguration.getCredentials();
    }
    Client client = clientOpt.get();
    ClientConfiguration configuration =
        ClientConfiguration.builder()
            .credentials(credentials)
            .encryptedFields(clientConfiguration.getFieldsToEncrypt())
            .environment(clientConfiguration.getEnvironment())
            .build();
    ClientConfiguration savedConfig = this.clientConfigurationRepository.save(configuration);
    client.getClientConfigurations().add(savedConfig);
    this.clientRepository.save(client);
    return builder.message("Added Client Config").payload(client).success(true).build();
  }
}
