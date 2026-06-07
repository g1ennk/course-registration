package hello.courseregistration.enrollment.service;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import hello.courseregistration.course.repository.CourseRepository;
import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import hello.courseregistration.enrollment.dto.response.EnrollmentResponse;
import hello.courseregistration.enrollment.dto.response.MyEnrollmentResponse;
import hello.courseregistration.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public EnrollmentResponse confirm(Long enrollmentId, Long classmateId) {
        Enrollment enrollment = ownedEnrollment(enrollmentId, classmateId);
        enrollment.confirm();
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public EnrollmentResponse cancel(Long enrollmentId, Long classmateId) {
        Enrollment enrollment = ownedEnrollment(enrollmentId, classmateId);
        enrollment.cancel();
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional(readOnly = true)
    public List<MyEnrollmentResponse> myEnrollments(Long classmateId) {
        // 조인 쿼리로 courseTitle까지 단일 쿼리 조회 (N+1 회피)
        return enrollmentRepository.findMyEnrollments(classmateId);
    }

    // 신청 검사 순서 헬퍼 메서드: 신청이 존재하는지(404) -> 소유자인지(403)
    private Enrollment ownedEnrollment(Long enrollmentId, Long classmateId) {
        // 404
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.ENROLLMENT_NOT_FOUND));
        // 403
        if (!enrollment.isOwnedBy(classmateId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인의 신청만 변경할 수 있습니다");
        }
        return enrollment;
    }


}
