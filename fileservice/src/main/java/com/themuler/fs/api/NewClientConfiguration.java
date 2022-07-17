package com.themuler.fs.api;

import lombok.Data;

import java.util.Map;

@Data
public class NewClientConfiguration {

  private String environment;

  private Boolean performEncryption;

  private String fieldsToEncrypt;

  private Map<String, String> credentials;
}
