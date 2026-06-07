package hello.courseregistration.enrollment.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EnrollmentApiTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    // capacity 지정 가능한 OPEN 강의 저장
    private Long openCourse(int capacity) {
        Course c = new Course(1L, "강의", "설명", 10000, capacity,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        c.changeStatusTo(CourseStatus.OPEN);
        return courseRepository.save(c).getId();
    }

    @Test
    void 없는_강의는_404() throws Exception {
        mockMvc.perform(post("/courses/999/enrollments").header("X-User-Id", 100))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
    }

    @Test
    void OPEN이_아니면_400() throws Exception {
        // DRAFT 강의 (open 안 함)
        Course c = new Course(1L, "강의", "설명", 10000, 10,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        Long id = courseRepository.save(c).getId();

        mockMvc.perform(post("/courses/" + id + "/enrollments").header("X-User-Id", 100))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COURSE_NOT_OPEN"));
    }

    @Test
    void 활성_중복_신청은_409() throws Exception {
        Long id = openCourse(10);
        enrollmentRepository.save(new Enrollment(id, 100L)); // 이미 PENDING

        mockMvc.perform(post("/courses/" + id + "/enrollments").header("X-User-Id", 100))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_ENROLLMENT"));
    }

    @Test
    void 정원_초과는_409() throws Exception {
        Long id = openCourse(1);
        enrollmentRepository.save(new Enrollment(id, 101L)); // 1자리 다 참

        mockMvc.perform(post("/courses/" + id + "/enrollments").header("X-User-Id", 100))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COURSE_FULL"));
    }

    @Test
    void 정상_신청은_201() throws Exception {
        Long id = openCourse(10);

        mockMvc.perform(post("/courses/" + id + "/enrollments").header("X-User-Id", 100))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(id))
                .andExpect(jsonPath("$.classmateId").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void X_User_Id_헤더가_없으면_400() throws Exception {
        Long id = openCourse(10);

        mockMvc.perform(post("/courses/" + id + "/enrollments"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void X_User_Id_헤더가_Long이_아니면_400() throws Exception {
        Long id = openCourse(10);

        mockMvc.perform(post("/courses/" + id + "/enrollments").header("X-User-Id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}