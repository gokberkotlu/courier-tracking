package com.gokberkotlu.couriertrackingapp.web;

import com.gokberkotlu.couriertrackingapp.exception.CourierNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Spring registers its own problem-detail advice at order 0; this one has to win over it.
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(CourierNotFoundException.class)
  public ProblemDetail handleCourierNotFound(CourierNotFoundException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationFailure(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
    problemDetail.setProperty("errors", errors);
    return problemDetail;
  }

  // courier_location has a unique constraint on (courier_id, recorded_at), so replaying a ping
  // that was already stored surfaces here rather than as a server error.
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDuplicateLocation(DataIntegrityViolationException exception) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "A location with this timestamp is already recorded for this courier");
  }
}
