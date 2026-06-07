package hello.courseregistration.enrollment.service;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import hello.courseregistration.course.repository.CourseRepository;
import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import hello.courseregistration.enrollment.dto.response.EnrollmentResponse;
import hello.courseregistration.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponse apply(Long courseId, Long classmateId) {
        // 1. 강의가 존재하는지 확인 (404)
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COURSE_NOT_FOUND));

        // 2. 강의가 열렸는지 확인 (400)
        if (course.getStatus() != CourseStatus.OPEN) {
            throw new ApiException(ErrorCode.COURSE_NOT_OPEN);
        }

        // 3. 이미 신청한 강의가 아닌지 확인 (409)
        if (enrollmentRepository.existsByCourseIdAndClassmateIdAndStatusIn(courseId, classmateId, EnrollmentStatus.ACTIVE)) {
            throw new ApiException(ErrorCode.DUPLICATE_ENROLLMENT);
        }

        // 4. 정원이 초과되지는 않았는지 확인 (409)
        long active = enrollmentRepository.countByCourseIdAndStatusIn(courseId, EnrollmentStatus.ACTIVE);
        if (active >= course.getCapacity()) {
            throw new ApiException(ErrorCode.COURSE_FULL);
        }

        // 5. 문제 없으면 INSERT
        Enrollment saved = enrollmentRepository.save(new Enrollment(courseId, classmateId));
        return EnrollmentResponse.from(saved);
    }

}
