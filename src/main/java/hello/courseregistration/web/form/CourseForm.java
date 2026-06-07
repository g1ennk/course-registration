package hello.courseregistration.web.form;

import hello.courseregistration.course.dto.request.CourseCreateRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 강의 등록 폼 바인딩용 가변 객체.
 * 검증 규칙은 API의 CourseCreateRequest와 동일하게 맞춰 th:errors로 인라인 표시한다.
 * (record는 폼 바인딩이 까다로워 web 계층은 가변 폼 객체를 둔다)
 */
@Getter
@Setter
public class CourseForm {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String description;

    @NotNull(message = "가격은 필수입니다")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다")
    private Integer price;

    @NotNull(message = "정원은 필수입니다")
    @Min(value = 1, message = "정원은 1 이상이어야 합니다")
    private Integer capacity;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    public CourseCreateRequest toRequest() {
        return new CourseCreateRequest(title, description, price, capacity, startDate, endDate);
    }
}
