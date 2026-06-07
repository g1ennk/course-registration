package hello.courseregistration.enrollment.dto.response;

import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        Long classmateId,
        EnrollmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(
                e.getId(), e.getCourseId(), e.getClassmateId(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
