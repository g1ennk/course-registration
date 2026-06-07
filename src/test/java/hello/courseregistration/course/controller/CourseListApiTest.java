package hello.courseregistration.course.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseListApiTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CourseRepository courseRepository;

    private Course course(String title) {
        return new Course(1L, title, "설명", 10000, 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
    }

    private void saveWithStatus(String title, CourseStatus status) {
        Course c = course(title);
        if (status == CourseStatus.OPEN) {
            c.changeStatusTo(CourseStatus.OPEN);
        } else if (status == CourseStatus.CLOSED) {
            c.changeStatusTo(CourseStatus.OPEN);
            c.changeStatusTo(CourseStatus.CLOSED);
        }
        courseRepository.save(c);
    }

    @Test
    void status_필터는_해당_상태만_반환하고_요약필드만_노출한다() throws Exception {
        saveWithStatus("오픈강의", CourseStatus.OPEN);
        saveWithStatus("마감강의", CourseStatus.CLOSED);
        saveWithStatus("초안강의", CourseStatus.DRAFT);

        mockMvc.perform(get("/courses").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("오픈강의"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                // 요약 DTO는 description·creatorId를 담지 않는다 (스펙 §2)
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].creatorId").doesNotExist());
    }

    @Test
    void status_생략시_DRAFT만_제외한_전체를_반환한다() throws Exception {
        saveWithStatus("오픈강의", CourseStatus.OPEN);
        saveWithStatus("마감강의", CourseStatus.CLOSED);
        saveWithStatus("초안강의", CourseStatus.DRAFT);

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].status").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.not("DRAFT"))));
    }

    @Test
    void status_DRAFT_요청은_400() throws Exception {
        mockMvc.perform(get("/courses").param("status", "DRAFT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void 잘못된_status_값은_400() throws Exception {
        mockMvc.perform(get("/courses").param("status", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
