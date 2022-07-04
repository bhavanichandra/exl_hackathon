package com.themuler.fs.repository;

import com.themuler.fs.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<User, Long> {}
