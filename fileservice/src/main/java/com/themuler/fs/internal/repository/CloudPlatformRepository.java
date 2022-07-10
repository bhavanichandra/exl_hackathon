package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.CloudPlatform;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CloudPlatformRepository extends CrudRepository<CloudPlatform, Integer> {
  Optional<CloudPlatform> findByName(String name);

}
