package com.vision.client.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TriggerResponse {

  private Result result;
  private Transaction transaction;

}
