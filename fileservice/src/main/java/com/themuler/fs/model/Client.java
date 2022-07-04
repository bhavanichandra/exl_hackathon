package com.themuler.fs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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

  private Boolean is_active;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "client")
  private List<ClientConfig> configs;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "client")
  private List<User> users;
}
