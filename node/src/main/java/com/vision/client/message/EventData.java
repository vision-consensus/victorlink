package com.vision.client.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventData {
  @JsonProperty("timeStamp")
  private long blockTimestamp;

  @JsonProperty("contractAddress")
  private String contractAddress;

  @JsonProperty("eventName")
  private String eventName;

  @JsonProperty("blockNumber")
  private int blockNumber;

  @JsonProperty("eventIndex")
  private int eventIndex;

  @JsonProperty("transactionId")
  private String transactionId;

  @JsonProperty("callerContractAddress")
  private String callerContractAddress;

  private Map<String, Object> result;

  @JsonProperty("resultType")
  private Map<String, Object> resultType;

  @JsonProperty("_unconfirmed")
  private boolean unconfirmed;

  @JsonProperty("topicMap")
  private Map<String, Object> topicMap;

  @JsonProperty("dataMap")
  private Map<String, Object> dataMap;
}