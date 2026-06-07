package hello.courseregistration.course.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseCreateApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final String VALID = """
            {"title":"스프링 부트 완전 정복","description":"JPA부터 배포까지",
             "price":99000,"capacity":30,"startDate":"2026-07-01","endDate":"2026-08-31"}
            """;

    @Test
    void 정상_등록은_201이고_요청자가_creator_DRAFT_상태가_된다() throws Exception {
        mockMvc.perform(post("/courses")
                        .header("X-User-Id", 7)
                        .contentType(APPLICATION_JSON)
                        .content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("스프링 부트 완전 정복"))
                .andExpect(jsonPath("$.price").value(99000))
                .andExpect(jsonPath("$.capacity").value(30))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.creatorId").value(7))
                .andExpect(jsonPath("$.createdAt").exists())
                // CourseResponse는 updatedAt을 의도적으로 제외한다 (스펙 §7.1)
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void 제목이_공백이면_400_VALIDATION_FAILED_필드상세를_담는다() throws Exception {
        String body = """
                {"title":"  ","price":1000,"capacity":10,"startDate":"2026-07-01","endDate":"2026-08-31"}
                """;
        mockMvc.perform(post("/courses")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("title")));
    }

    @Test
    void 가격이_음수면_400_VALIDATION_FAILED() throws Exception {
        String body = """
                {"title":"강의","price":-1,"capacity":10,"startDate":"2026-07-01","endDate":"2026-08-31"}
                """;
        mockMvc.perform(post("/courses")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("price")));
    }

    @Test
    void 정원이_0이면_400_VALIDATION_FAILED() throws Exception {
        String body = """
                {"title":"강의","price":1000,"capacity":0,"startDate":"2026-07-01","endDate":"2026-08-31"}
                """;
        mockMvc.perform(post("/courses")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("capacity")));
    }

    @Test
    void 시작일이_종료일보다_늦으면_400_INVALID_REQUEST_빈_errors() throws Exception {
        // 단일 필드는 모두 유효 → @Valid 통과 후 도메인 생성자 교차검증에서 INVALID_REQUEST
        String body = """
                {"title":"강의","price":1000,"capacity":10,"startDate":"2026-09-01","endDate":"2026-07-01"}
                """;
        mockMvc.perform(post("/courses")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors", hasSize(0)));
    }
}
