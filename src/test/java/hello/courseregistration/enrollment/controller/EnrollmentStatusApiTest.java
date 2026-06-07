package hello.courseregistration.enrollment.controller;

import hello.courseregistration.enrollment.domain.Enrollment;
import hello.courseregistration.enrollment.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EnrollmentStatusApiTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    // classmate=100 의 PENDING 신청 저장
    private Long pendingEnrollment() {
        return enrollmentRepository.save(new Enrollment(1L, 100L)).getId();
    }

    // ===== confirm =====
    @Test
    void confirm_없는_신청은_404() throws Exception {
        mockMvc.perform(patch("/enrollments/999/confirm").header("X-User-Id", 100))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENROLLMENT_NOT_FOUND"));
    }

    @Test
    void confirm_소유자가_아니면_403() throws Exception {
        Long id = pendingEnrollment();
        mockMvc.perform(patch("/enrollments/" + id + "/confirm").header("X-User-Id", 999))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void confirm_PENDING이_아니면_400() throws Exception {
        Enrollment e = new Enrollment(1L, 100L);
        e.confirm(); // 이미 CONFIRMED (결제 완료된 신청)
        Long id = enrollmentRepository.save(e).getId();
        mockMvc.perform(patch("/enrollments/" + id + "/confirm").header("X-User-Id", 100))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void confirm_정상은_200_CONFIRMED() throws Exception {
        Long id = pendingEnrollment();
        mockMvc.perform(patch("/enrollments/" + id + "/confirm").header("X-User-Id", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // ===== cancel =====
    @Test
    void cancel_없는_신청은_404() throws Exception {
        mockMvc.perform(patch("/enrollments/999/cancel").header("X-User-Id", 100))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENROLLMENT_NOT_FOUND"));
    }

    @Test
    void cancel_소유자가_아니면_403() throws Exception {
        Long id = pendingEnrollment();
        mockMvc.perform(patch("/enrollments/" + id + "/cancel").header("X-User-Id", 999))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void cancel_이미_취소면_400() throws Exception {
        Enrollment e = new Enrollment(1L, 100L);
        e.cancel(); // 이미 CANCELLED (결제 취소가 완료된 신청)
        Long id = enrollmentRepository.save(e).getId();
        mockMvc.perform(patch("/enrollments/" + id + "/cancel").header("X-User-Id", 100))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void cancel_정상은_200_CANCELLED() throws Exception {
        Long id = pendingEnrollment();
        mockMvc.perform(patch("/enrollments/" + id + "/cancel").header("X-User-Id", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}