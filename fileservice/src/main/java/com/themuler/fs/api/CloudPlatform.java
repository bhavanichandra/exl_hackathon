package com.themuler.fs.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CloudPlatform {
  AWS("aws"),
  AZURE("azure"),
  GOOGLE_CLOUD_PLATFORM("gcp");

  private final String cloudPlatform;

  CloudPlatform(String value) {
    this.cloudPlatform = value;
  }

  public String getCloudPlatform() {
    return this.cloudPlatform;
  }

  public static List<String> toValues() {
    return Arrays.stream(CloudPlatform.values())
        .map(CloudPlatform::getCloudPlatform)
        .collect(Collectors.toList());
  }
}
