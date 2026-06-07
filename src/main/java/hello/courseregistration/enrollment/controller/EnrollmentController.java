package hello.courseregistration.enrollment.controller;

import hello.courseregistration.enrollment.dto.response.EnrollmentResponse;
import hello.courseregistration.enrollment.dto.response.MyEnrollmentResponse;
import hello.courseregistration.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PatchMapping("/enrollments/{enrollmentId}/confirm")
    public EnrollmentResponse confirm(
            @PathVariable Long enrollmentId,
            @RequestHeader("X-User-Id") Long classmateId) {
        return enrollmentService.confirm(enrollmentId, classmateId);
    }

    @PatchMapping("/enrollments/{enrollmentId}/cancel")
    public EnrollmentResponse cancel(
            @PathVariable Long enrollmentId,
            @RequestHeader("X-User-Id") Long classmateId) {
        return enrollmentService.cancel(enrollmentId, classmateId);
    }

    @GetMapping("/enrollments/me")
    public List<MyEnrollmentResponse> myEnrollments(
            @RequestHeader("X-User-Id") Long classmateId) {
        return enrollmentService.myEnrollments(classmateId);
    }

}
