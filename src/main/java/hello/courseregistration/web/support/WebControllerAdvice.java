package hello.courseregistration.web.support;

import hello.courseregistration.common.exception.ApiException;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * web(SSR) 계층 공통 어드바이스.
 * <ul>
 *   <li>모든 뷰에 현재 사용자(currentUserId)를 주입한다.</li>
 *   <li>GET 조회나 잘못된 파라미터에서 발생한 예외가 글로벌 {@code @RestControllerAdvice}(JSON)로
 *       새어 브라우저에 JSON이 뜨는 것을 막고, HTML 에러 페이지로 렌더한다.
 *       (POST 액션의 비즈니스 예외는 각 컨트롤러가 try/catch로 같은 페이지에 flash로 표시한다)</li>
 * </ul>
 * HIGHEST_PRECEDENCE로 두어 web 컨트롤러에서는 글로벌 JSON 핸들러보다 먼저 선택되게 한다.
 * (basePackages로 web에만 적용되므로 REST 컨트롤러는 그대로 글로벌 JSON 핸들러를 쓴다)
 */
@ControllerAdvice(basePackages = "hello.courseregistration.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebControllerAdvice {

    @ModelAttribute("currentUserId")
    public Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionKeys.USER_ID);
    }

    // 없는 강의(404), DRAFT 목록 요청(400) 등 GET 경로의 비즈니스 예외
    @ExceptionHandler(ApiException.class)
    public ModelAndView handleApi(ApiException e) {
        return errorView(e.getErrorCode().getStatus(), e.getMessage());
    }

    // 잘못된 status/target/userId 등 파라미터 타입 변환 실패 (핸들러 진입 전 발생 → try/catch 불가)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return errorView(HttpStatus.BAD_REQUEST, e.getName() + " 값의 형식이 올바르지 않습니다");
    }

    private ModelAndView errorView(HttpStatus status, String message) {
        ModelAndView mv = new ModelAndView("error");
        mv.setStatus(status);
        mv.addObject("status", status.value());
        mv.addObject("reason", status.getReasonPhrase());
        mv.addObject("message", message);
        return mv;
    }
}
