package hello.courseregistration.enrollment.controller;

import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.repository.CourseRepository;
import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyEnrollmentNPlusOneTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    EntityManager em;

    @Test
    void 내_신청_목록_조회는_신청_수와_무관하게_단일_쿼리다() throws Exception {
        int n = 12;
        for (int i = 0; i < n; i++) {
            Long courseId = courseRepository.save(new Course(1L, "강의" + i, "설명", 10000, 10,
                    LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31))).getId();
            enrollmentRepository.save(new Enrollment(courseId, 100L));
        }
        em.flush();
        em.clear(); // 1차 캐시 비우기 → 실무처럼 매 조회가 DB를 치게

        Statistics stats = em.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        mockMvc.perform(get("/enrollments/me").header("X-User-Id", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(n));

        // 조인 projection이면 1쿼리. naive(항목마다 findById)면 1 + N = 13.
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }
}
