package hello.courseregistration.course.dto.response;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;

public record StatusResponse(
        Long id,
        CourseStatus status
) {
    public static StatusResponse from(Course course) {
        return new StatusResponse(course.getId(), course.getStatus());
    }
}
