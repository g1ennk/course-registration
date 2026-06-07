package hello.courseregistration.web;

import hello.courseregistration.web.support.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@Controller
public class UserWebController {

    // 상단바에서 현재 사용자(userId)를 세션에 저장하고, 보던 페이지로 돌아간다.
    @PostMapping("/web/user")
    public String change(@RequestParam(required = false) Long userId,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (userId == null) {
            ra.addFlashAttribute("error", "userId를 입력하세요");
        } else {
            session.setAttribute(SessionKeys.USER_ID, userId);
            ra.addFlashAttribute("message", "현재 사용자를 " + userId + "(으)로 설정했습니다");
        }
        return "redirect:" + safeBack(referer);
    }

    // Referer의 경로(path)만 취해 같은 출처 내에서만 복귀한다 (open-redirect 방지). 없거나 비정상이면 목록으로.
    private String safeBack(String referer) {
        if (referer != null) {
            try {
                String path = URI.create(referer).getPath();
                if (path != null && path.startsWith("/web/")) {
                    return path;
                }
            } catch (RuntimeException ignored) {
                // 잘못된 Referer는 기본 경로로
            }
        }
        return "/web/courses";
    }
}
