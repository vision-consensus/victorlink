package com.vision.web.entity;

import lombok.Data;

@Data
public class TaskParams {
  private String get;
  private String path;
  private Long times;
  private String pair;
  private String pool;
  private String vrc20;
  private String publicKey;
  private String type;
  private Long version;
}
