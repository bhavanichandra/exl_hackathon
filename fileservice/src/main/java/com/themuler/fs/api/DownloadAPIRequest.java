package com.themuler.fs.api;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@Getter
@Setter
public class DownloadAPIRequest {

  @NotNull @NotBlank private String fileName;

  @Nullable  private String bucketName;

  @Nullable private Long vfs_id;
}
