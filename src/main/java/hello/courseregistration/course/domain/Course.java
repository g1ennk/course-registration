package hello.courseregistration.course.domain;

import hello.courseregistration.course.common.exception.IllegalStateTransitionException;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Course {

    private Long creatorId;
    private String title;
    private String description;
    private int price;
    private int capacity;
    private LocalDate startDate;
    private LocalDate endDate;
    private CourseStatus status;

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
            throw new IllegalStateTransitionException("DRAFT 상태에서만 OPEN으로 전이할 수 있습니다.");
        }
        this.status = CourseStatus.OPEN;
    }

    public void close() {
        if (status != CourseStatus.OPEN) {
            throw new IllegalStateTransitionException("OPEN 상태에서만 CLOSED로 전이할 수 있습니다.");
        }
        this.status = CourseStatus.CLOSED;
    }

    public boolean isOwnedBy(Long userId) {
        return creatorId.equals(userId);
    }
}
