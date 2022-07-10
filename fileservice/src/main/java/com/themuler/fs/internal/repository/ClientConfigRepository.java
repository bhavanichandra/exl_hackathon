package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.ClientConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientConfigRepository extends CrudRepository<ClientConfig, Long> {
}
