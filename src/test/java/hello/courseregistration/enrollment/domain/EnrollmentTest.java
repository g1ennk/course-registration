package hello.courseregistration.enrollment.domain;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentTest {

    private Enrollment pendingEnrollment() {
        return new Enrollment(1L, 100L); // courseId, classmateId
    }

    @Test
    void 새_신청은_PENDING() {
        assertThat(pendingEnrollment().getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    void confirm은_PENDING에서_CONFIRMED으로() {
        Enrollment e = pendingEnrollment();
        e.confirm();

        assertThat(e.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    void PENDING_아닌데_confirm하면_예외() {
        Enrollment e = pendingEnrollment();
        e.confirm();
        assertThatThrownBy(e::confirm)
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    void CANCELLED를_confirm하면_예외() {
        Enrollment e = pendingEnrollment();
        e.cancel();                        // PENDING → CANCELLED
        assertThatThrownBy(e::confirm)
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    void cancel은_PENDING에서_CANCELLED로() {
        Enrollment e = pendingEnrollment(); // 초기 PENDING
        e.cancel();

        assertThat(e.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    void cancel은_CONFIRMED에서도_CANCELLED로() {
        Enrollment e = pendingEnrollment();
        e.confirm();                          // PENDING → CONFIRMED
        e.cancel();                           // CONFIRMED → CANCELLED

        assertThat(e.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    void 이미_CANCELLED를_cancel하면_예외() {
        Enrollment e = pendingEnrollment();
        e.cancel();                        // PENDING → CANCELLED
        assertThatThrownBy(e::cancel)      // 다시 cancel → 예외
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    void 소유자_판별() {
        Enrollment e = pendingEnrollment(); // classmateId = 100L

        assertThat(e.isOwnedBy(100L)).isTrue();
        assertThat(e.isOwnedBy(999L)).isFalse();
    }

    @Test
    void 활성_판별() {
        // PENDING = ACTIVE
        Enrollment pending = pendingEnrollment();
        assertThat(pending.isActive()).isTrue();

        // CONFIRMED = ACTIVE
        Enrollment confirmed = pendingEnrollment();
        confirmed.confirm();
        assertThat(confirmed.isActive()).isTrue();

        // CANCELLED = INACTIVE
        Enrollment cancelled = pendingEnrollment();
        cancelled.cancel();
        assertThat(cancelled.isActive()).isFalse();
    }
}