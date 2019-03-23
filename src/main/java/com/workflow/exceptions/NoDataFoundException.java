package com.workflow.exceptions;

public class NoDataFoundException extends RuntimeException {

    private String message;

    public NoDataFoundException(String description)
    {
        this.message= description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
