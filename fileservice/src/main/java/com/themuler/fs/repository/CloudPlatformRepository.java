package com.themuler.fs.repository;

import com.themuler.fs.model.CloudPlatform;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CloudPlatformRepository extends CrudRepository<CloudPlatform, Integer> {
  Optional<CloudPlatform> findByName(String name);

}
