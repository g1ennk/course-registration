package hello.courseregistration.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record CourseCreateRequest(
        @NotBlank(message = "제목은 필수입니다")
        String title,

        String description, // 선택 (Nullable)

        @NotNull(message = "가격은 필수입니다")
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다")
        Integer price,

        @NotNull(message = "정원은 필수입니다")
        @Min(value = 1, message = "정원은 1 이상이어야 합니다")
        Integer capacity,

        @NotNull(message = "시작일은 필수입니다")
        LocalDate startDate,

        @NotNull(message = "종료일은 필수입니다")
        LocalDate endDate
) {
}
