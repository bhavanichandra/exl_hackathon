package com.themuler.fs.internal.service.user;

import com.themuler.fs.api.LoginResponse;
import com.themuler.fs.api.LoginUser;
import com.themuler.fs.api.NewUser;
import com.themuler.fs.api.ResponseWrapper;
import com.themuler.fs.internal.model.AppUser;
import com.themuler.fs.internal.model.VirtualFileSystem;

import java.util.List;

public interface UserServiceInterface {

  ResponseWrapper<AppUser> saveUser(NewUser user);

  ResponseWrapper<List<AppUser>> getAllUsers();

  ResponseWrapper<AppUser> newSuperUser(LoginUser user);

  ResponseWrapper<AppUser> getUserById(String id);

  ResponseWrapper<LoginResponse> validateUser(String username, String password);

  ResponseWrapper<List<VirtualFileSystem>> getUploadedFileDetails(String user_id);
}
