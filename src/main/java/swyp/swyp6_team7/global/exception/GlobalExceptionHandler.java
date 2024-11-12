package swyp.swyp6_team7.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        // 로그 출력
        System.out.println("예외 발생: " + ex.getMessage());

        // 클라이언트에게 서버 에러 메시지 전송
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> bindException(BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}
