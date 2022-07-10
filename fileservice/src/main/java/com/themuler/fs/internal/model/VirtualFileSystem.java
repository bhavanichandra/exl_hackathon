package com.themuler.fs.internal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "vfs")
public class VirtualFileSystem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String status;

  private String type;

  private String path;

  private String fileName;

  @ManyToOne
  private CloudPlatform cloudPlatform;

  @OneToMany
  @JoinColumn(name = "parent_vfs_id")
  private List<VirtualFileSystem> parent;
}
