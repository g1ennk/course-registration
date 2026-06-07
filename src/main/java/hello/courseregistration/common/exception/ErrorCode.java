package hello.courseregistration.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 전이입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다"),
    COURSE_NOT_OPEN(HttpStatus.BAD_REQUEST, "OPEN 상태의 강의만 신청할 수 있습니다"),
    DUPLICATE_ENROLLMENT(HttpStatus.CONFLICT, "이미 신청한 강의입니다"),
    COURSE_FULL(HttpStatus.CONFLICT, "정원이 초과되었습니다");

    private final HttpStatus status;
    private final String message;
}
