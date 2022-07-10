package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
  Role getByName(String name);
}
