package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.Client;
import com.themuler.fs.internal.model.ClientConfig;
import com.themuler.fs.internal.model.CloudPlatform;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientConfigRepository extends CrudRepository<ClientConfig, Long> {

  Iterable<ClientConfig> findByClientAndCloudPlatform(Client client, CloudPlatform cloudPlatform);

  Iterable<ClientConfig> findAllByClient(Client client);
}
