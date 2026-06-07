package hello.courseregistration.course.repository;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course course() {
        return new Course(1L, "강의", "설명", 10000, 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
    }

    @Test
    void 공개목록은_DRAFT를_제외한다() {
        // DRAFT
        courseRepository.save(course());

        // OPEN
        Course open = course();
        open.open();
        courseRepository.save(open);

        // CLOSED
        Course closed = course();
        closed.open();
        closed.close();
        courseRepository.save(closed);

        List<Course> result =
                courseRepository.findByStatusNotOrderByCreatedAtDesc(CourseStatus.DRAFT);

        assertThat(result).extracting(Course::getStatus)
                .containsExactlyInAnyOrder(CourseStatus.OPEN, CourseStatus.CLOSED);
    }

    @Test
    void 공개목록은_createdAt_내림차순_최신순으로_반환한다() throws InterruptedException {
        Course older = course();
        older.open();
        courseRepository.save(older); // 먼저 저장 = 더 오래됨

        Thread.sleep(10); // createdAt이 분명히 달라지도록 간격 보장

        Course newer = course();
        newer.open();
        courseRepository.save(newer); // 나중 저장 = 최신

        List<Course> result =
                courseRepository.findByStatusNotOrderByCreatedAtDesc(CourseStatus.DRAFT);

        // 방향(desc)을 못박는다 — Asc로 바뀌면 빨강
        assertThat(result).extracting(Course::getCreatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(result.get(0).getId()).isEqualTo(newer.getId());
    }

    @Test
    void status_필터_파생쿼리는_해당_상태만_최신순으로_조회한다() throws InterruptedException {
        Course open1 = course();
        open1.open();
        courseRepository.save(open1);

        Thread.sleep(10);

        Course open2 = course();
        open2.open();
        courseRepository.save(open2);

        Course closed = course();
        closed.open();
        closed.close();
        courseRepository.save(closed);

        List<Course> result =
                courseRepository.findByStatusOrderByCreatedAtDesc(CourseStatus.OPEN);

        assertThat(result).extracting(Course::getStatus).containsOnly(CourseStatus.OPEN);
        assertThat(result.get(0).getId()).isEqualTo(open2.getId()); // 최신 먼저
    }
}
