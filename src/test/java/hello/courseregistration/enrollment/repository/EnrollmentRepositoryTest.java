package hello.courseregistration.enrollment.repository;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.repository.CourseRepository;
import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import hello.courseregistration.enrollment.dto.response.MyEnrollmentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;

    private Long saveCourse(String title) {
        Course c = new Course(1L, title, "설명", 10000, 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        return courseRepository.save(c).getId();
    }

    @Test
    void 활성_신청만_센다() {
        // courseId = 1 -> PENDING, CONFIRMED, CANCELLED 각각 1건으로 총 3건
        // PENDING
        enrollmentRepository.save(new Enrollment(1L, 100L));

        // CONFIRMED
        Enrollment confirmed = new Enrollment(1L, 101L);
        confirmed.confirm();
        enrollmentRepository.save(confirmed);

        // CANCELLED
        Enrollment cancelled = new Enrollment(1L, 102L);
        cancelled.cancel();
        enrollmentRepository.save(cancelled);

        // courseId = 2 -> 타 강의로 집계에 섞이면 안됨
        // PENDING
        enrollmentRepository.save(new Enrollment(2L, 100L));

        // 활성 상태 개수 = 2건
        long count = enrollmentRepository.countByCourseIdAndStatusIn(
                1L, EnrollmentStatus.ACTIVE
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    void 활성_중복_신청을_판별한다() {
        // courseId = 1, classmateId = 100, PENDING (활성 상태)
        enrollmentRepository.save(new Enrollment(1L, 100L));

        // courseId = 1, classmateId = 101, CANCELLED (비활성 상태)
        Enrollment cancelled = new Enrollment(1L, 101L);
        cancelled.cancel();
        enrollmentRepository.save(cancelled);

        // id가 100인 수강생은 활성 신청이 있음(PENDING)
        assertThat(enrollmentRepository.existsByCourseIdAndClassmateIdAndStatusIn(1L, 100L, EnrollmentStatus.ACTIVE)).isTrue();

        // id가 101인 수강생은 활성 신청이 없음(CANCELLED)
        assertThat(enrollmentRepository.existsByCourseIdAndClassmateIdAndStatusIn(1L, 101L, EnrollmentStatus.ACTIVE)).isFalse();

        // 신청 이력이 없는 사용자는 활성 신청 불가능
        assertThat(enrollmentRepository.existsByCourseIdAndClassmateIdAndStatusIn(1L, 999L, EnrollmentStatus.ACTIVE)).isFalse();
    }

    @Test
    void 내_신청을_courseTitle과_함께_최신순으로_조회한다() {
        Long spring = saveCourse("스프링");
        Long react = saveCourse("리액트");

        // 나의 신청 2건
        enrollmentRepository.save(new Enrollment(spring, 100L)); // 스프링 = PENDING

        Enrollment cancelled = new Enrollment(react, 100L); // 리액트 = CANCEL
        cancelled.cancel();
        enrollmentRepository.save(cancelled); // 나중 = 최신 = 리액트

        // 남의 신청 1건
        enrollmentRepository.save(new Enrollment(spring, 999L));

        List<MyEnrollmentResponse> result = enrollmentRepository.findMyEnrollments(100L);

        // classmateId 필터 — 내 것만 2건
        assertThat(result).hasSize(2);

        // 조인으로 courseTitle 채움
        assertThat(result).extracting(MyEnrollmentResponse::courseTitle)
                .containsExactlyInAnyOrder("스프링", "리액트");

        // CANCELLED 포함
        assertThat(result).extracting(MyEnrollmentResponse::status)
                .contains(EnrollmentStatus.CANCELLED);

        // 최신순 (createdAt 내림차순)
        assertThat(result).isSortedAccordingTo(
                Comparator.comparing(MyEnrollmentResponse::createdAt).reversed());
    }

}
