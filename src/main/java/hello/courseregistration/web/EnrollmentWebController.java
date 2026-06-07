package hello.courseregistration.web;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.enrollment.service.EnrollmentService;
import hello.courseregistration.web.support.SessionKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class EnrollmentWebController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    public String enroll(@PathVariable Long courseId,
                         @SessionAttribute(value = SessionKeys.USER_ID, required = false) Long userId,
                         RedirectAttributes ra) {
        if (userId == null) {
            ra.addFlashAttribute("error", "현재 사용자를 먼저 설정하세요");
            return "redirect:/web/courses/" + courseId;
        }
        try {
            enrollmentService.apply(courseId, userId);
            ra.addFlashAttribute("message", "수강 신청 완료 (PENDING)");
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/courses/" + courseId;
    }

    @GetMapping("/enrollments/me")
    public String my(@SessionAttribute(value = SessionKeys.USER_ID, required = false) Long userId, Model model) {
        model.addAttribute("enrollments",
                userId == null ? List.of() : enrollmentService.myEnrollments(userId));
        return "enrollments/my";
    }

    @PostMapping("/enrollments/{id}/confirm")
    public String confirm(@PathVariable Long id,
                          @SessionAttribute(value = SessionKeys.USER_ID, required = false) Long userId,
                          RedirectAttributes ra) {
        return changeState(id, userId, ra, true);
    }

    @PostMapping("/enrollments/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @SessionAttribute(value = SessionKeys.USER_ID, required = false) Long userId,
                         RedirectAttributes ra) {
        return changeState(id, userId, ra, false);
    }

    private String changeState(Long id, Long userId, RedirectAttributes ra, boolean confirm) {
        if (userId == null) {
            ra.addFlashAttribute("error", "현재 사용자를 먼저 설정하세요");
            return "redirect:/web/enrollments/me";
        }
        try {
            if (confirm) {
                enrollmentService.confirm(id, userId);
                ra.addFlashAttribute("message", "확정(CONFIRMED) 처리했습니다");
            } else {
                enrollmentService.cancel(id, userId);
                ra.addFlashAttribute("message", "취소(CANCELLED) 처리했습니다");
            }
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/enrollments/me";
    }
}
