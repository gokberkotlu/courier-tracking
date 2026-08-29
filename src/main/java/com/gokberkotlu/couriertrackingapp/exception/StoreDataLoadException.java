package com.gokberkotlu.couriertrackingapp.exception;

public class StoreDataLoadException extends RuntimeException {
  public StoreDataLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public StoreDataLoadException(String message) {
    super(message);
  }
}
