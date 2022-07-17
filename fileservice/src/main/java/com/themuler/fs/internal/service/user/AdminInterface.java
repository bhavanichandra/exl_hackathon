package com.themuler.fs.internal.service.user;

import com.themuler.fs.api.NewClient;
import com.themuler.fs.api.NewClientConfiguration;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.Client;

import java.util.List;

public interface AdminInterface {

  ResponseWrapper<Client> addClient(NewClient client);

  ResponseWrapper<Client> addClientConfiguration(
      String clientId, NewClientConfiguration clientConfiguration);

  ResponseWrapper<List<Client>> getAllClients();

  ResponseWrapper<Client> getClientById(String id);
}
