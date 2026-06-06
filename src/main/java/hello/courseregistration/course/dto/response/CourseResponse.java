package hello.courseregistration.course.dto.response;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String title,
        String description,
        int price,
        int capacity,
        LocalDate startDate,
        LocalDate endDate,
        CourseStatus status,
        Long creatorId,
        LocalDateTime createdAt
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(), course.getTitle(), course.getDescription(),
                course.getPrice(), course.getCapacity(),
                course.getStartDate(), course.getEndDate(),
                course.getStatus(), course.getCreatorId(), course.getCreatedAt()
        );
    }
}
