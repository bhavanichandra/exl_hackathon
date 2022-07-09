package com.themuler.fs.service;

import com.themuler.fs.model.ResponseWrapper;
import com.themuler.fs.model.User;

import java.util.List;

public interface UserServiceInterface {

  ResponseWrapper<User> saveUser(User user);

  ResponseWrapper<List<User>> getAllUsers();

  ResponseWrapper<User> getUserById(long id);
}
