package com.vision.web.entity;

import lombok.Data;

@Data
public class TaskSpecRequest {
  private Long confirmations;
  private String type;
  private TaskParams params;

}