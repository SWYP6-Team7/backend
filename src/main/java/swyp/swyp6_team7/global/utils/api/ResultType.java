package swyp.swyp6_team7.global.utils.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum ResultType {
    SUCCESS,
    HTTP_TIMEOUT,
    FAIL,
    BAD_REQUEST,
    INTERNAL_ERROR;

    private ResultType() {
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    @JsonIgnore
    public boolean isFail() {
        return !this.isSuccess();
    }
}