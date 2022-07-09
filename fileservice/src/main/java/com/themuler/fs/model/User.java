package com.themuler.fs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "app_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String email;

  @JsonIgnore
  private String password;

  private Boolean is_active;

  @ManyToOne(fetch = FetchType.EAGER)
  private Client client;

  @OneToOne(fetch = FetchType.EAGER)
  private Role role;
}
