package com.workflow.exceptions;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    // other exception handlers

    @ExceptionHandler(NoDataFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(NoDataFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(GenericRuntimeException.class)
    protected ResponseEntity<Object> handleGenericException(GenericRuntimeException ex) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> baseException(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> baseRunTimeException(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}