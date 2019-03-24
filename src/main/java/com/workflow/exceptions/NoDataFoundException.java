package com.workflow.exceptions;

public class NoDataFoundException extends RuntimeException {

    private String message;
    private String WFId;
    private String CId;

    public String getWFId() {
        return WFId;
    }

    public void setWFId(String WFId) {
        this.WFId = WFId;
    }

    public NoDataFoundException(String WFId, String CId, String description)
    {
        this.message = description;
        this.CId = CId;
        this.WFId = WFId;
    }

    public String getMessage() {
        return message + " for " + CId + ".";
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
