package com.themuler.fs.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum UserRole {
  SUPER_ADMIN {
    public List<Feature> allowedFeatures() {
      return Arrays.asList(
          Feature.GET_ALL_USER,
          Feature.ADD_ANY_USER,
          Feature.UPDATE_ANY_USER,
          Feature.REMOVE_ANY_USER,
          Feature.ANY_FILE_DOWNLOAD,
          Feature.ANY_FILE_UPLOAD,
          Feature.ANY_FILE_REMOVE,
          Feature.FULL_ACCESS);
    }
  },
  CLIENT_ADMIN {
    public List<Feature> allowedFeatures() {
      return Arrays.asList(
          Feature.GET_CLIENT_SPECIFIC_USER,
          Feature.ADD_CLIENT_SPECIFIC_USER,
          Feature.UPDATE_CLIENT_SPECIFIC_USER,
          Feature.REMOVE_CLIENT_SPECIFIC_USER,
          Feature.CLIENT_SPECIFIC_FILE_DOWNLOAD,
          Feature.CLIENT_SPECIFIC_FILE_UPLOAD,
          Feature.CLIENT_SPECIFIC_FILE_REMOVE,
          Feature.CLIENT_SPECIFIC_ACCESS);
    }
  },
  USER {
    public List<Feature> allowedFeatures() {
      return Arrays.asList(
          Feature.USER_SPECIFIC_FILE_DOWNLOAD,
          Feature.USER_SPECIFIC_FILE_UPLOAD,
          Feature.USER_SPECIFIC_FILE_REMOVE,
          Feature.USER_SPECIFIC_ACCESS);
    }
  },

  TEMP_USER {
    public List<Feature> allowedFeatures() {
      return Arrays.asList(Feature.USER_SPECIFIC_FILE_DOWNLOAD, Feature.TEMP_USER_ACCESS);
    }
  };

  public List<Feature> allowedFeatures() {
    return new ArrayList<>();
  }
}
