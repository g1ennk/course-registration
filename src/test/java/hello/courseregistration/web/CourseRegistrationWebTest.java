package hello.courseregistration.web;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import hello.courseregistration.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseRegistrationWebTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CourseRepository courseRepository;

    private Long saveCourse(Long creatorId, CourseStatus status) {
        Course c = new Course(creatorId, "강의", "설명", 10000, 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        if (status == CourseStatus.OPEN) {
            c.changeStatusTo(CourseStatus.OPEN);
        }
        return courseRepository.save(c).getId();
    }

    @Test
    void 목록_화면은_200이고_courses_list_뷰를_렌더한다() throws Exception {
        mockMvc.perform(get("/web/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"));
    }

    @Test
    void 목록은_데이터가_있으면_record_DTO_필드를_렌더한다() throws Exception {
        saveCourse(1L, CourseStatus.OPEN); // price 10000

        mockMvc.perform(get("/web/courses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("10000"))) // c.price() 렌더 = record 접근자 동작
                .andExpect(content().string(containsString("OPEN")));
    }

    @Test
    void 상세는_정원_잔여와_비소유자용_신청버튼을_렌더한다() throws Exception {
        Long id = saveCourse(1L, CourseStatus.OPEN);

        mockMvc.perform(get("/web/courses/" + id)) // 세션 사용자 없음 → 비소유자 분기
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("10000")))   // course.price() 렌더
                .andExpect(content().string(containsString("수강 신청"))); // 비소유자 분기
    }

    @Test
    void 소유자가_아니면_상태변경은_에러_플래시와_함께_상세로_리다이렉트한다() throws Exception {
        Long id = saveCourse(1L, CourseStatus.DRAFT);

        mockMvc.perform(post("/web/courses/" + id + "/status")
                        .param("target", "OPEN")
                        .sessionAttr("userId", 2L)) // 소유자(1) 아님
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/courses/" + id))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void 수강신청_성공은_성공_플래시와_함께_상세로_리다이렉트한다() throws Exception {
        Long id = saveCourse(1L, CourseStatus.OPEN);

        mockMvc.perform(post("/web/courses/" + id + "/enroll")
                        .sessionAttr("userId", 100L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/courses/" + id))
                .andExpect(flash().attribute("message", containsString("신청")));
    }

    // ===== 에러 경로: 글로벌 JSON advice가 아니라 web HTML error 뷰로 가야 한다 =====

    @Test
    void 없는_강의_상세는_JSON이_아니라_HTML_error_뷰를_렌더한다() throws Exception {
        mockMvc.perform(get("/web/courses/99999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    void DRAFT_필터_요청은_error_뷰를_렌더한다() throws Exception {
        mockMvc.perform(get("/web/courses").param("status", "DRAFT"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"));
    }

    @Test
    void 잘못된_status_값은_error_뷰를_렌더한다() throws Exception {
        mockMvc.perform(get("/web/courses").param("status", "GARBAGE"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"));
    }

    // ===== 등록 폼: 검증 실패 재렌더 / 성공 리다이렉트 =====

    @Test
    void 등록_검증_실패는_폼을_다시_렌더하고_필드오류를_담는다() throws Exception {
        mockMvc.perform(post("/web/courses").sessionAttr("userId", 1L)
                        .param("title", "")            // 공백 → @NotBlank
                        .param("price", "1000")
                        .param("capacity", "10")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/form"))
                .andExpect(model().attributeHasFieldErrors("form", "title"));
    }

    @Test
    void 등록_성공은_상세로_리다이렉트한다() throws Exception {
        mockMvc.perform(post("/web/courses").sessionAttr("userId", 1L)
                        .param("title", "테스트강의")
                        .param("price", "1000")
                        .param("capacity", "10")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/web/courses/*"));
    }
}
