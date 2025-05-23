package swyp.swyp6_team7.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.api.ErrorMessage;
import swyp.swyp6_team7.global.utils.api.ResultType;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 모든 예외 처리
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleAllExceptions(Exception e) {
        log.error("Global exception caught: {}", e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(
                "서버 에러가 발생했습니다.",
                "Internal Server Error"
        ).setErrorType(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ApiResponse.error(ResultType.INTERNAL_ERROR, errorMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResponse<String> bindException(BindException e) {
        ErrorMessage errorMessage = new ErrorMessage(
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                "Bad Request"
        ).setErrorType(HttpStatus.BAD_REQUEST.value());

        log.warn("Request Valid Error - message: {}", errorMessage);
        return ApiResponse.error(errorMessage);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(MoingAuthenticationException.class)
    @ResponseBody
    public ApiResponse<String> handleAuthenticationException(MoingAuthenticationException e) {
        log.error("Moing Authentication Exception: {}", e.getMessage());
        ErrorMessage message = new ErrorMessage(
                e.getMessage(),
                "인증 에러가 발생했습니다."
        );

        return ApiResponse.error(ResultType.UNAUTHORIZED, message);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserBlockException.class)
    @ResponseBody
    public ApiResponse<String> handleUserBlockException(UserBlockException e) {
        log.error("User Block Exception: {}", e.getMessage());
        ErrorMessage message = new ErrorMessage(
                e.getMessage(),
                e.getErrorCode().name()
        );

        return ApiResponse.error(ResultType.ACCESS_DENIED, message);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MoingApplicationException.class)
    @ResponseBody
    public ApiResponse<String> handleApplicationException(MoingApplicationException e) {
        log.error("Moing Application Exception: {}", e.getMessage());
        ErrorMessage message = new ErrorMessage(
                e.getMessage(),
                "처리 중 에러가 발생했습니다."
        );

        return ApiResponse.error(ResultType.SUCCESS, message);
    }
}
