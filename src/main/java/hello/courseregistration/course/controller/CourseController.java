package hello.courseregistration.course.controller;

import hello.courseregistration.course.dto.request.CourseCreateRequest;
import hello.courseregistration.course.dto.response.CourseResponse;
import hello.courseregistration.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(
            @RequestHeader("X-User-Id") Long creatorId,
            @Valid @RequestBody CourseCreateRequest request) {
        return courseService.create(creatorId, request);
    }
}
