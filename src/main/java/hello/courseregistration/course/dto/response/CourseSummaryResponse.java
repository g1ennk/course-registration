package hello.courseregistration.course.dto.response;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;

import java.time.LocalDateTime;

public record CourseSummaryResponse(
        Long id,
        String title,
        int price,
        int capacity,
        CourseStatus status,
        LocalDateTime createdAt
) {
    public static CourseSummaryResponse from(Course course) {
        return new CourseSummaryResponse(
                course.getId(), course.getTitle(),
                course.getPrice(), course.getCapacity(),
                course.getStatus(), course.getCreatedAt()
        );
    }
}
