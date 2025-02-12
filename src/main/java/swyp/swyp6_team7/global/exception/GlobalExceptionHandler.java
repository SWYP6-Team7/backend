package swyp.swyp6_team7.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> bindException(BindException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        log.warn("Request Valid Error - message: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
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
