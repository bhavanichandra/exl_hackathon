package com.themuler.fs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "client_config")
public class ClientConfig {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String environment;

  @ManyToOne private Client client;

  @OneToOne private CloudPlatform cloudPlatform;

  @Type(type = "jsonb")
  private Map<String, Object> credential;
}
