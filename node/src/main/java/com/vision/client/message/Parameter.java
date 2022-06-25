package com.vision.client.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Parameter {

  @JsonProperty("type_url")
  private String typeUrl;
  private Map<String, String> value;
}
