package com.vision.web.entity;

import lombok.Data;

import java.util.Date;

@Data
public class VisionTx {
  private Long id;
  private String taskRunId;
  private String surrogateId;
  private String from;
  private String to;
  private String data;
  private Long value;
  private String hash;
  private Long confirmed;
  private Long sentAt;
  private String signedRawTx;
  private Date createdAt;
  private Date updatedAt;
}
