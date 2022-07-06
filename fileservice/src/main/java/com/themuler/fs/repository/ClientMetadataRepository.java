package com.themuler.fs.repository;

import com.themuler.fs.model.ClientMetadata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientMetadataRepository extends CrudRepository<ClientMetadata, Long> {}
