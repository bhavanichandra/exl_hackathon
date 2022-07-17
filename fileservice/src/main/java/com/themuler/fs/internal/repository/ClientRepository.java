package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.Client;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends MongoRepository<Client, ObjectId> {
  Client findByName(String name);
}
