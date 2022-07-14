package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {
  Client findByName(String name);
}
