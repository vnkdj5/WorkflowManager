package com.workflow.exceptions;

import java.util.Date;

public class GenericRuntimeException extends RuntimeException {
    private Date timestamp;
    private String message;

    public GenericRuntimeException(String WFId, String name, String notAvailable) {
        this.timestamp = new Date();
        this.message = WFId + " " + name + "-" + notAvailable;
    }

    public GenericRuntimeException(String WFId, String msg) {
        this.timestamp = new Date();
        this.message = WFId + " : " + msg;
    }

    public GenericRuntimeException(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
