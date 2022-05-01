package com.vision.common;

public class VisionException extends Exception {
  public VisionException() {
    super();
  }

  public VisionException(String message) {
    super(message);
  }

  public VisionException(String message, Throwable cause) {
    super(message, cause);
  }
}
