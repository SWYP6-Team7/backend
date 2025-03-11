package swyp.swyp6_team7.global.exception;

public enum ErrorCode {
    ACCOUNT_LOCKED("계정이 차단되었습니다.");

    private final String reason;

    ErrorCode(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
