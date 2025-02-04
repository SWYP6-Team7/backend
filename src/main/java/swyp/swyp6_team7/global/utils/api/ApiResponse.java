package swyp.swyp6_team7.global.utils.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public class ApiResponse<S> {
    private ResultType resultType;
    private S result;
    private ErrorMessage error;

    public ApiResponse(S result) {
        this.result = result;
        this.error = null;
        this.resultType = ResultType.SUCCESS;
    }

    public ApiResponse(ErrorMessage error) {
        this(error, ResultType.FAIL);
    }

    public ApiResponse(ErrorMessage error, ResultType resultType) {
        this.error = error;
        this.resultType = resultType;
        this.result = null;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.resultType.isSuccess();
    }

    @JsonIgnore
    public boolean isFail() {
        return this.resultType.isFail();
    }

    public ResultType getResultType() {
        return this.resultType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ErrorMessage getError() {
        return this.error;
    }

    @JsonIgnore
    public S getResult() {
        if (this.isFail()) {
            this.throwNewApiResponseException();
        }

        return this.result;
    }

    @JsonProperty("success")
    public S getResultForJson() {
        return this.result;
    }

    private ApiResponse() {
    }

    private void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    @JsonProperty
    private void setResult(S result) {
        this.result = result;
    }

    private void setError(ErrorMessage error) {
        this.error = error;
    }

    public String toString() {
        return "ApiResponse [resultType=" + resultType + ", result=" + result + ", error=" + error + "]";
    }

    public static <S> @NotNull ApiResponse<S> success(@Nullable S result) {
        return new ApiResponse(result);
    }

    public static <S> @NotNull ApiResponse<S> error(@Nullable ErrorMessage error) {
        return new ApiResponse(error);
    }

    public static <S> @NotNull ApiResponse<S> error(
            @Nullable ResultType resultType,
            @Nullable ErrorMessage error
    ) {
        if (error == null) {
            throw new IllegalArgumentException("error cannot be null");
        } else if (resultType == null) {
            throw new IllegalArgumentException("resultType cannot be null");
        } else {
            return new ApiResponse(error, resultType);
        }
    }

    private void throwNewApiResponseException() {
        throw new NoApiSuccessResultException(this.error);
    }

    public static class NoApiSuccessResultException extends ApiResponseException {
        public NoApiSuccessResultException(ErrorMessage error) {
            super(error);
        }
    }
}