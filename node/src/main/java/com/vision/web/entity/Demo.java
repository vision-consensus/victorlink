package com.vision.web.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class Demo implements Serializable {

  private Long id;

  @NotBlank(message = "key cannot be null")
  private String key;

  private String value;

  private String create_time;
  private String update_time;
}
