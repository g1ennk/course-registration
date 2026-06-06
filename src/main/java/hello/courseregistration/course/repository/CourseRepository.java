package hello.courseregistration.course.repository;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // 특정 상태만 조회 (최신순) — status 파라미터가 주어진 경우
    List<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status);

    // 공개 목록: DRAFT(초안) 제외 전체 조회 (최신순) — status 생략 시
    List<Course> findByStatusNotOrderByCreatedAtDesc(CourseStatus status);
}
