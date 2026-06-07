package hello.courseregistration.course.controller;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseStatusApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CourseRepository courseRepository;

    private Long savedCourseId(Long creatorId) {
        Course c = new Course(creatorId, "강의", "설명", 10000, 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        return courseRepository.save(c).getId();
    }

    @Test
    void 없는_강의는_404() throws Exception {
        mockMvc.perform(patch("/courses/999/status")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
    }

    @Test
    void 소유자가_아니면_403() throws Exception {
        Long id = savedCourseId(1L);
        mockMvc.perform(patch("/courses/" + id + "/status")
                        .header("X-User-Id", 999)
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void 소유자의_정상_전이는_200() throws Exception {
        Long id = savedCourseId(1L);
        mockMvc.perform(patch("/courses/" + id + "/status")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void DRAFT로_변경은_400() throws Exception {
        Long id = savedCourseId(1L);
        mockMvc.perform(patch("/courses/" + id + "/status")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"DRAFT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void 잘못된_status_값_본문은_400() throws Exception {
        Long id = savedCourseId(1L);
        mockMvc.perform(patch("/courses/" + id + "/status")
                        .header("X-User-Id", 1)
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"FOO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
