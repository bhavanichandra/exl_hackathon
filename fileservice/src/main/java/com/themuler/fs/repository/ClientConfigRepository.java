package com.themuler.fs.repository;

import com.themuler.fs.model.ClientConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientConfigRepository extends CrudRepository<ClientConfig, Long> {
}
