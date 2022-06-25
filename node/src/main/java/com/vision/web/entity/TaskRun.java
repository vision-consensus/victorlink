package com.vision.web.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TaskRun {
  private String id;
  private String jobRunID;
  private String result;
  private String error;
  private Long taskSpecId;
  private int status;
  private Long minimumConfirmations;
  private Long confirmations;
  private Long payment;
  private Integer level;
  private Date createdAt;
  private Date updatedAt;
  private String type;
}
