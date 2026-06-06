package hello.courseregistration.enrollment.domain;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "enrollment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;
    private Long classmateId;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Enrollment(Long courseId, Long classmateId) {
        this.courseId = courseId;
        this.classmateId = classmateId;
        this.status = EnrollmentStatus.PENDING;
    }

    public void confirm() {
        if (status != EnrollmentStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION, "PENDING 상태에서만 CONFIRMED로 전이할 수 있습니다.");
        }
        this.status = EnrollmentStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == EnrollmentStatus.CANCELLED) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION, "이미 취소된 신청입니다.");
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
