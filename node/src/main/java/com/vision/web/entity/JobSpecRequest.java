package com.vision.web.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class JobSpecRequest {
  private List<InitiatorRequest> initiators;
  private List<TaskSpecRequest> tasks;
  private Long minPayment;
  private Date startAt;
  private Date endAt;

}