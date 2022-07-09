package com.themuler.fs.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseWrapper<T> {
  private String message;
  private Boolean success;
  private T payload;
}
