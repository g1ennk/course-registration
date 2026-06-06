package hello.courseregistration.enrollment.domain;

import hello.courseregistration.common.exception.IllegalStateTransitionException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Enrollment {

    private Long courseId;
    private Long classmateId;
    private EnrollmentStatus status;

    public Enrollment(Long courseId, Long classmateId) {
        this.courseId = courseId;
        this.classmateId = classmateId;
        this.status = EnrollmentStatus.PENDING;
    }

    public void confirm() {
        if (status != EnrollmentStatus.PENDING) {
            throw new IllegalStateTransitionException("PENDING 상태에서만 CONFIRMED로 전이할 수 있습니다.");
        }
        this.status = EnrollmentStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateTransitionException("이미 취소된 신청입니다.");
        }
        this.status = EnrollmentStatus.CANCELLED;
    }

    public boolean isOwnedBy(Long classmateId) {
        return Objects.equals(this.classmateId, classmateId);
    }

    public boolean isActive() {
        return status == EnrollmentStatus.PENDING || status == EnrollmentStatus.CONFIRMED;
    }

}
