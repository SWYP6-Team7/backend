package swyp.swyp6_team7.global.utils.api;

public class ErrorMessage {
    private int errorType = 0;
    private String reason;
    private String title;

    public ErrorMessage(String reason, String title) {
        this.reason = reason;
        this.title = title;
    }

    private ErrorMessage(String reason) {
        this.reason = reason;
    }

    private ErrorMessage() {
    }

    public String getReason() {
        return this.reason;
    }

    private void setReason(String reason) {
        this.reason = reason;
    }

    public ErrorMessage setErrorType(int errorType) {
        this.errorType = errorType;
        return this;
    }

    public ErrorMessage setTitle(String title) {
        this.title = title;
        return this;
    }

    public String toString() {
        return "ErrorMessage [errorType=" + errorType + ", reason=" + reason + ", title=" + title + "]";
    }
}