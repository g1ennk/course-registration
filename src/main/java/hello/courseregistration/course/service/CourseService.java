package hello.courseregistration.course.service;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import hello.courseregistration.course.dto.request.CourseCreateRequest;
import hello.courseregistration.course.dto.response.CourseDetailResponse;
import hello.courseregistration.course.dto.response.CourseResponse;
import hello.courseregistration.course.dto.response.CourseSummaryResponse;
import hello.courseregistration.course.dto.response.StatusResponse;
import hello.courseregistration.course.repository.CourseRepository;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import hello.courseregistration.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public CourseResponse create(Long creatorId, CourseCreateRequest request) {
        Course course = new Course(
                creatorId, request.title(), request.description(),
                request.price(), request.capacity(),
                request.startDate(), request.endDate()
        );
        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getList(CourseStatus status) {
        if (status == CourseStatus.DRAFT) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "DRAFT는 목록 조회에서 허용되지 않습니다");
        }
        List<Course> courses = (status == null)
                ? courseRepository.findByStatusNotOrderByCreatedAtDesc(CourseStatus.DRAFT)
                : courseRepository.findByStatusOrderByCreatedAtDesc(status);
        return courses.stream()
                .map(CourseSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COURSE_NOT_FOUND));

        int enrolledCount = (int) enrollmentRepository.countByCourseIdAndStatusIn(
                courseId, EnrollmentStatus.ACTIVE);
        int remaining = course.getCapacity() - enrolledCount;

        return CourseDetailResponse.from(course, enrolledCount, remaining);
    }

    @Transactional
    public StatusResponse changeStatus(Long courseId, Long userId, CourseStatus target) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COURSE_NOT_FOUND));   // 404
        if (!course.isOwnedBy(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "강의 소유자만 상태를 변경할 수 있습니다");  // 403
        }
        course.changeStatusTo(target);   // 전이 불법/DRAFT → 400
        return StatusResponse.from(course);
    }
}
