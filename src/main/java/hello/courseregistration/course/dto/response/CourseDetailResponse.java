package hello.courseregistration.course.dto.response;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CourseDetailResponse(
        Long id,
        String title,
        String description,
        int price,
        int capacity,
        LocalDate startDate,
        LocalDate endDate,
        CourseStatus status,
        Long creatorId,
        int enrolledCount,
        int remaining,
        LocalDateTime createdAt
) {
    public static CourseDetailResponse from(Course course, int enrolledCount, int remaining) {
        return new CourseDetailResponse(
                course.getId(), course.getTitle(), course.getDescription(),
                course.getPrice(), course.getCapacity(),
                course.getStartDate(), course.getEndDate(),
                course.getStatus(), course.getCreatorId(),
                enrolledCount, remaining, course.getCreatedAt()
        );
    }
}
