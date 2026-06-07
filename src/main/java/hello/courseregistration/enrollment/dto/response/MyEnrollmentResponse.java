package hello.courseregistration.enrollment.dto.response;

import hello.courseregistration.enrollment.domain.EnrollmentStatus;

import java.time.LocalDateTime;

public record MyEnrollmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        LocalDateTime createdAt
) {
}
