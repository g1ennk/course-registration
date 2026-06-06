package hello.courseregistration.course.domain;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "course")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Course(Long creatorId, String title, String description, int price, int capacity, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "시작일은 종료일보다 늦을 수 없습니다");
        }
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = CourseStatus.DRAFT;
    }

    public void open() {
        if (status != CourseStatus.DRAFT) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION, "DRAFT 상태에서만 OPEN으로 전이할 수 있습니다.");
        }
        this.status = CourseStatus.OPEN;
    }

    public void close() {
        if (status != CourseStatus.OPEN) {
            throw new ApiException(ErrorCode.INVALID_STATE_TRANSITION, "OPEN 상태에서만 CLOSED로 전이할 수 있습니다.");
        }
        this.status = CourseStatus.CLOSED;
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(creatorId, userId);
    }
}
