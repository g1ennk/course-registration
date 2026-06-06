package hello.courseregistration.common.response;

import hello.courseregistration.common.exception.ErrorCode;
import org.springframework.validation.BindingResult;

import java.util.List;

public record ErrorResponse(String code, String message, List<FieldError> errors) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, List.of());
    }

    public static ErrorResponse ofValidation(BindingResult bindingResult) {
        List<FieldError> errors = bindingResult.getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        return new ErrorResponse(ErrorCode.VALIDATION_FAILED.name(), ErrorCode.VALIDATION_FAILED.getMessage(), errors);
    }

    public record FieldError(String field, String message) {
    }
}
