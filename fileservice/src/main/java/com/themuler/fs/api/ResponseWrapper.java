package com.themuler.fs.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWrapper<T> {
  private String message;
  private Boolean success;
  private T payload;
}
