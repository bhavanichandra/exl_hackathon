package com.themuler.fs.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@Getter
public class NewUser {

  @Null private String id;

  @NotNull @NotBlank private String name;

  @NotNull @NotBlank private String email;

  @NotNull @NotBlank private String password;

  @NotNull @NotBlank private UserRole role;

  @JsonProperty("client")
  private String client;
}
