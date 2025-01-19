package swyp.swyp6_team7.global.utils.api;

import org.apache.commons.lang3.StringUtils;

public class ApiResponseException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public ApiResponseException(ErrorMessage errorMessage) {
        super(getMessageWithDefault(errorMessage));
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return this.errorMessage;
    }

    private static String getMessageWithDefault(ErrorMessage errorMessage) {
        return errorMessage != null && StringUtils.isNotEmpty(errorMessage.getReason())
                ? errorMessage.getReason() : "Response result is empty.";
    }
}