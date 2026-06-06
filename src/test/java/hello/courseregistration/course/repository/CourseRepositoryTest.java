package hello.courseregistration.course.repository;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
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
}
