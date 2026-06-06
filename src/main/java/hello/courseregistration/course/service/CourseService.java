package hello.courseregistration.course.service;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.dto.request.CourseCreateRequest;
import hello.courseregistration.course.dto.response.CourseResponse;
import hello.courseregistration.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    public CourseResponse create(Long creatorId, CourseCreateRequest request) {
        Course course = new Course(
                creatorId, request.title(), request.description(),
                request.price(), request.capacity(),
                request.startDate(), request.endDate()
        );
        return CourseResponse.from(courseRepository.save(course));
    }
}
