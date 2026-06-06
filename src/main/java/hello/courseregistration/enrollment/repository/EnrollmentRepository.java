package hello.courseregistration.enrollment.repository;

import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    long countByCourseIdAndStatusIn(Long courseId, List<EnrollmentStatus> statuses);
}
