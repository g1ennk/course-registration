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

    private Long creatorId;
    private String title;
    private String description;
    private int price;
    private int capacity;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Course(Long creatorId, String title, String description, int price, int capacity, LocalDate startDate, LocalDate endDate) {
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
