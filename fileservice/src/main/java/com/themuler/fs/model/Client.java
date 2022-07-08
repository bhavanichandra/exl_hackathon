package com.themuler.fs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "client")
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String description;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "client")
  private List<User> users;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "client")
  private List<ClientConfig> clientConfigs;
}
