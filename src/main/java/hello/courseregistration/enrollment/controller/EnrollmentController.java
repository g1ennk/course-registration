package hello.courseregistration.enrollment.controller;

import hello.courseregistration.enrollment.dto.response.EnrollmentResponse;
import hello.courseregistration.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse apply(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long classmateId) {
        return enrollmentService.apply(courseId, classmateId);
    }

}
