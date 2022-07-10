package com.themuler.fs.internal.service.user;

import com.themuler.fs.api.NewUser;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.User;

import java.util.List;

public interface UserServiceInterface {

  ResponseWrapper<User> saveUser(NewUser user);

  ResponseWrapper<List<User>> getAllUsers();

  ResponseWrapper<User> getUserById(long id);

  ResponseWrapper<User> validateUser(String username, String password);
}
