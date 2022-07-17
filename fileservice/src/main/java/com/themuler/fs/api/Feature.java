package com.themuler.fs.api;

public enum Feature {
  GET_ALL_USER,
  ADD_ANY_USER,
  UPDATE_ANY_USER,
  REMOVE_ANY_USER,
  GET_CLIENT_SPECIFIC_USER,
  ADD_CLIENT_SPECIFIC_USER,
  UPDATE_CLIENT_SPECIFIC_USER,
  REMOVE_CLIENT_SPECIFIC_USER,
  CLIENT_SPECIFIC_FILE_DOWNLOAD,
  CLIENT_SPECIFIC_FILE_UPLOAD,
  CLIENT_SPECIFIC_FILE_REMOVE,
  CLIENT_SPECIFIC_ACCESS,
  USER_SPECIFIC_FILE_DOWNLOAD,
  USER_SPECIFIC_FILE_UPLOAD,
  USER_SPECIFIC_FILE_REMOVE,
  USER_SPECIFIC_ACCESS,
  TEMP_DOWNLOAD_ACCESS,

  CONFIGURE_CLOUD_PLATFORM,

  ADD_NEW_CONFIGURATIONS,

  ADD_NEW_CLIENTS,
  GET_CLIENTS,
  GET_CLIENT_BY_ID,

  GET_VFS_DATA
}
