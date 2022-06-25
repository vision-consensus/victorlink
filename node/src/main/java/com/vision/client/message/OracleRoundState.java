package com.vision.client.message;

import lombok.Data;

import java.math.BigInteger;

@Data
public class OracleRoundState {
  private Boolean eligibleToSubmit;
  private long roundId;
  private BigInteger latestSubmission;
  private long startedAt;
  private long timeout;
  private BigInteger availableFunds;
  private int oracleCount;
  private BigInteger paymentAmount;
}
