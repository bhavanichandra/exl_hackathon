package com.themuler.fs.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "client_metadata")
public class ClientMetadata {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "metadata")
  private List<ClientConfig> clientConfigs;

  private String environment;

  @ManyToOne
  private Client client;

}
