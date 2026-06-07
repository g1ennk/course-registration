package hello.courseregistration.course.controller;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import hello.courseregistration.course.repository.CourseRepository;
import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.repository.EnrollmentRepository;
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
class CourseDetailApiTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    void 상세는_활성신청만_센_enrolledCount와_remaining을_반환한다() throws Exception {
        Course c = new Course(1L, "강의", "설명", 10000, 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        c.changeStatusTo(CourseStatus.OPEN);
        Long id = courseRepository.save(c).getId();

        enrollmentRepository.save(new Enrollment(id, 100L)); // PENDING (활성)

        Enrollment confirmed = new Enrollment(id, 101L);
        confirmed.confirm();
        enrollmentRepository.save(confirmed); // CONFIRMED (활성)

        Enrollment cancelled = new Enrollment(id, 102L);
        cancelled.cancel();
        enrollmentRepository.save(cancelled); // CANCELLED (집계 제외)

        mockMvc.perform(get("/courses/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.capacity").value(10))
                .andExpect(jsonPath("$.enrolledCount").value(2)) // PENDING+CONFIRMED, CANCELLED 제외
                .andExpect(jsonPath("$.remaining").value(8))      // 10 - 2
                .andExpect(jsonPath("$.creatorId").value(1))
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void 없는_강의_상세는_404() throws Exception {
        mockMvc.perform(get("/courses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
    }
}
