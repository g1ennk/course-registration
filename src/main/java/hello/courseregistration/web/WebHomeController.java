package hello.courseregistration.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebHomeController {

    // 루트 접속 시 강의 목록 화면으로 보낸다 (REST API 경로와 무관)
    @GetMapping("/")
    public String home() {
        return "redirect:/web/courses";
    }
}
