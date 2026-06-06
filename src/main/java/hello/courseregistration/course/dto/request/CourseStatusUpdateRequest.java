package hello.courseregistration.course.dto.request;

import hello.courseregistration.course.domain.CourseStatus;
import jakarta.validation.constraints.NotNull;

public record CourseStatusUpdateRequest(
        @NotNull(message = "상태는 필수입니다")
        CourseStatus status
) {
}
