package swyp.swyp6_team7.global.exception;

public class UserBlockException extends MoingAuthenticationException {
    private ErrorCode errorCode;

    public UserBlockException(String message) {
        super(message);
    }

    public UserBlockException(ErrorCode errorCode) {
        super(errorCode.getReason());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
