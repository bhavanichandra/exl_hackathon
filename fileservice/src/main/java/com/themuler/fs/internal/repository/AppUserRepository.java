package com.themuler.fs.internal.repository;

import com.themuler.fs.internal.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<User, Long> {

  User findByEmail(String email);
}
