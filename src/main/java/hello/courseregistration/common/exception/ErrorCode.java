package hello.courseregistration.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 전이입니다");

    private final HttpStatus status;
    private final String message;
}
