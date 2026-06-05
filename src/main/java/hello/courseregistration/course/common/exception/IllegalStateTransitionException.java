package hello.courseregistration.course.common.exception;

import org.springframework.http.HttpStatus;

public class IllegalStateTransitionException extends ApiException {
    public IllegalStateTransitionException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_STATE_TRANSITION", message);
    }
}
