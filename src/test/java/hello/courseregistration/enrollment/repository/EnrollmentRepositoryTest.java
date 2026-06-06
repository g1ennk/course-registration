package hello.courseregistration.enrollment.repository;

import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

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
                1L, List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED)
        );

        assertThat(count).isEqualTo(2);
    }

}
