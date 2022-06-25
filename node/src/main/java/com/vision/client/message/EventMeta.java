package com.vision.client.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class EventMeta {
  private long at;
  @JsonProperty("page_size")
  private int pageSize;
  private String fingerprint;
  private Map<String, String> links;
}