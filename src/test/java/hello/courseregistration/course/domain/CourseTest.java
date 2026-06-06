package hello.courseregistration.course.domain;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseTest {
    private Course draftCourse() {
        return new Course(1L, "Spring", "desc", 50000, 2,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
    }

    // ===== 상태 전이: open / close =====

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
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    void CLOSED를_open하면_예외() {
        Course c = draftCourse();
        c.open();
        c.close(); // DRAFT → OPEN → CLOSED
        assertThatThrownBy(c::open)
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
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
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    void CLOSED를_close하면_예외() {
        Course c = draftCourse();
        c.open();
        c.close(); // DRAFT → OPEN → CLOSED
        assertThatThrownBy(c::close)
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    // ===== 상태 전이: changeStatusTo (target 디스패치) =====

    @Test
    void changeStatusTo_OPEN이면_OPEN으로() {
        Course c = draftCourse();
        c.changeStatusTo(CourseStatus.OPEN);
        assertThat(c.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    void changeStatusTo_CLOSED면_CLOSED로() {
        Course c = draftCourse();
        c.open(); // DRAFT → OPEN
        c.changeStatusTo(CourseStatus.CLOSED);
        assertThat(c.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    void changeStatusTo_DRAFT면_예외() {
        Course c = draftCourse();
        c.open(); // OPEN
        assertThatThrownBy(() -> c.changeStatusTo(CourseStatus.DRAFT))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    // ===== 기간 불변식 (생성자) =====

    @Test
    void 시작일이_종료일보다_늦으면_생성_불가() {
        assertThatThrownBy(() -> new Course(1L, "Spring", "desc", 50000, 2,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 7, 1)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void 시작일과_종료일이_같으면_생성_가능() {
        LocalDate sameDay = LocalDate.of(2026, 7, 1);
        assertThat(new Course(1L, "Spring", "desc", 50000, 2, sameDay, sameDay).getStatus())
                .isEqualTo(CourseStatus.DRAFT);
    }

    // ===== 소유권 =====

    @Test
    void 소유자_판별() {
        Course c = draftCourse();          // creatorId = 1L
        assertThat(c.isOwnedBy(1L)).isTrue();
        assertThat(c.isOwnedBy(2L)).isFalse();
    }
}
