package com.themuler.fs.internal.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "client_config")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ClientConfig {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String environment;

  @ManyToOne private Client client;

  @OneToOne private CloudPlatform cloudPlatform;

  @Type(type = "jsonb")
  @Column(name = "credential", columnDefinition = "jsonb")
  private Map<String, Object> credential;


}
