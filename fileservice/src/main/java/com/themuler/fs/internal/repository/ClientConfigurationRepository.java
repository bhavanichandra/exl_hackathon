package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.ClientConfiguration;
import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

public interface ClientConfigurationRepository extends CrudRepository<ClientConfiguration, ObjectId> {}
