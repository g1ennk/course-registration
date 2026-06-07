package hello.courseregistration.web;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.course.domain.CourseStatus;
import hello.courseregistration.course.dto.response.CourseResponse;
import hello.courseregistration.course.service.CourseService;
import hello.courseregistration.web.form.CourseForm;
import hello.courseregistration.web.support.SessionKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web/courses")
@RequiredArgsConstructor
public class CourseWebController {

    private final CourseService courseService;

    @GetMapping
    public String list(@RequestParam(required = false) CourseStatus status, Model model) {
        model.addAttribute("courses", courseService.getList(status));
        model.addAttribute("status", status);
        // 공개 목록 필터 옵션 (DRAFT 제외)
        model.addAttribute("statuses", List.of(CourseStatus.OPEN, CourseStatus.CLOSED));
        return "courses/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getDetail(id));
        return "courses/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new CourseForm());
        return "courses/form";
    }

    @PostMapping
    public String create(@SessionAttribute(value = SessionKeys.USER_ID, required = false) Long userId,
                         @Valid @ModelAttribute("form") CourseForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes ra) {
        if (userId == null) {
            ra.addFlashAttribute("error", "현재 사용자를 먼저 설정하세요");
            return "redirect:/web/courses/new";
        }
        if (binding.hasErrors()) {
            return "courses/form"; // 필드 검증 오류는 th:errors로 인라인 표시
        }
        try {
            CourseResponse created = courseService.create(userId, form.toRequest());
            ra.addFlashAttribute("message", "강의 등록 완료 (#" + created.id() + ")");
            return "redirect:/web/courses/" + created.id();
        } catch (ApiException e) {
            // 교차검증(시작일 > 종료일) 등 비즈니스 오류는 alert로
            model.addAttribute("error", e.getMessage());
            return "courses/form";
        }
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam CourseStatus target,
                               @SessionAttribute(value = SessionKeys.USER_ID, required = false) Long userId,
                               RedirectAttributes ra) {
        if (userId == null) {
            ra.addFlashAttribute("error", "현재 사용자를 먼저 설정하세요");
            return "redirect:/web/courses/" + id;
        }
        try {
            courseService.changeStatus(id, userId, target);
            ra.addFlashAttribute("message", "상태를 " + target + "(으)로 변경했습니다");
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/courses/" + id;
    }
}
