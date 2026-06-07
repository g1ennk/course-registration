package hello.courseregistration.enrollment.domain;

import java.util.List;

public enum EnrollmentStatus {
    PENDING, CONFIRMED, CANCELLED;

    // 정원을 점유하는 활성 상태
    public static final List<EnrollmentStatus> ACTIVE = List.of(PENDING, CONFIRMED);
}
