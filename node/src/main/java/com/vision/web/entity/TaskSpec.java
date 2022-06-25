package com.vision.web.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TaskSpec implements Serializable {
  private Long id;
  private String jobSpecID;
  private Long confirmations;
  private String type;
  private String params;
  private Integer level;
  private Date createdAt;
  private Date updatedAt;
  private Date deletedAt;
}
