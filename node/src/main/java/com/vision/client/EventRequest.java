package com.vision.client;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class EventRequest {
  private long blockNum;
  private String jobId;
  private String requester;
  private String callbackAddr;
  private String callbackFunctionId;
  private long cancelExpiration;
  private String data;
  private long dataVersion;
  private String requestId;
  private BigInteger payment;
  private String contractAddr;
}
