package hello.courseregistration.course;

import hello.courseregistration.course.common.exception.IllegalStateTransitionException;
import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseTest {
    private Course draftCourse() {
        return new Course(1L, "Spring", "desc", 50000, 2,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
    }

    @Test
    void 새_강의는_DRAFT() {
        assertThat(draftCourse().getStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    void open은_DRAFT에서만() {
        Course c = draftCourse();
        c.open();
        assertThat(c.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    void 이미_OPEN을_open하면_예외() {
        Course c = draftCourse();
        c.open();
        assertThatThrownBy(c::open)
                .isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    void close는_OPEN에서만() {
        Course c = draftCourse();
        c.open();
        c.close();
        assertThat(c.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    void DRAFT를_close하면_예외() {
        Course c = draftCourse();   // DRAFT 상태 (open 안 함)
        assertThatThrownBy(c::close)
                .isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    void 소유자_판별() {
        Course c = draftCourse();          // creatorId = 1L
        assertThat(c.isOwnedBy(1L)).isTrue();
        assertThat(c.isOwnedBy(2L)).isFalse();
    }
}
