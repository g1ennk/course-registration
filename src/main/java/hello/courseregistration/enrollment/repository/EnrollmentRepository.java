package hello.courseregistration.enrollment.repository;

import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import hello.courseregistration.enrollment.dto.response.MyEnrollmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    long countByCourseIdAndStatusIn(Long courseId, List<EnrollmentStatus> statuses);

    boolean existsByCourseIdAndClassmateIdAndStatusIn(Long courseId, Long classmateId, List<EnrollmentStatus> statuses);

    // 내 신청 목록 — Course와 연관관계가 없어 courseId로 ad-hoc 조인, 생성자 표현식으로 courseTitle까지 단일 쿼리에 담아 N+1 회피
    @Query("""
            select new hello.courseregistration.enrollment.dto.response.MyEnrollmentResponse(
                e.id, e.courseId, c.title, e.status, e.createdAt)
            from Enrollment e join Course c on c.id = e.courseId
            where e.classmateId = :classmateId
            order by e.createdAt desc
            """)
    List<MyEnrollmentResponse> findMyEnrollments(@Param("classmateId") Long classmateId);
}
