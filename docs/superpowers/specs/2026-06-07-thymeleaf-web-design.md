# Thymeleaf SSR 웹 (번외) 설계

작성일: 2026-06-07 · 브랜치: `feat/thymeleaf-web` (제출본 `main`은 무수정)

## 목표
이미 만든 수강신청 API 서버 위에, 같은 앱 안에서 서버사이드 렌더링(Thymeleaf) 화면을 얹어
브라우저로 전체 흐름을 조작한다. 학습 목적의 번외 작업.

## 결정 사항
- **통합 방식**: MVC `@Controller`가 기존 `CourseService`/`EnrollmentService`를 직접 주입해 호출 (in-process).
  서비스/도메인/리포지토리는 무수정.
- **위치**: 별도 브랜치. 제출본 main은 깨끗한 API 서버로 유지.
- **범위**: 풀세트 (수강생 + 크리에이터 + 검증 에러 표시).
- **현재 사용자**: `HttpSession`의 `userId`. 상단바에서 변경. 헤더(`X-User-Id`) 대신 세션이 식별자 역할.
- **스타일**: Bootstrap 5 CDN.
- **테스트**: 가벼운 스모크(MockMvc) 2~3개.

## 라우팅 (REST API와 충돌 피하려 `/web` prefix)
| 화면/액션 | 라우트 |
|---|---|
| 홈 → 목록 redirect | `GET /` |
| 강의 목록(상태 필터) | `GET /web/courses` |
| 강의 상세 | `GET /web/courses/{id}` |
| 강의 등록 폼 / 등록 | `GET /web/courses/new` / `POST /web/courses` |
| 상태 변경 | `POST /web/courses/{id}/status` |
| 수강 신청 | `POST /web/courses/{id}/enroll` |
| 내 신청 목록 | `GET /web/enrollments/me` |
| 확정 / 취소 | `POST /web/enrollments/{id}/confirm` / `/cancel` |
| 현재 사용자 변경 | `POST /web/user` |

## 컴포넌트
- `web/CourseWebController`, `web/EnrollmentWebController`, `web/UserWebController`
- `web/support/WebModelAdvice` — `@ControllerAdvice(basePackages="...web")`로 모든 web 뷰에 `currentUserId` 주입
- `web/form/CourseForm` — 가변 폼 객체 + Bean Validation (Thymeleaf 바인딩용)
- `templates/fragments/layout.html` (head/topbar/alerts), `templates/courses/*`, `templates/enrollments/my.html`

## 에러 처리 (핵심)
기존 `@RestControllerAdvice`(JSON)가 뷰 요청을 가로채지 않도록:
- **필드 검증**: 폼에 `@Valid` + `BindingResult` 파라미터 → 예외 대신 결과 바인딩 → `th:errors` 인라인.
- **비즈니스 예외(ApiException: 404/403/409/400)**: web 컨트롤러에서 try/catch → `RedirectAttributes` flash로
  Bootstrap alert 표시 후 redirect. (예외가 advice까지 전파되지 않으므로 JSON 응답 안 됨)

## 범위 밖 (YAGNI)
진짜 인증/비밀번호, 페이지네이션/검색, JS 인터랙션, 별도 디자인 시스템.
- **CSRF**: Spring Security를 쓰지 않으므로 상태 변경 POST에 CSRF 토큰이 없다. 실제 인증을 붙일 때 함께 도입(의식적 생략).

## 에러 처리 보강 (리뷰 반영)
GET 조회/파라미터 타입 오류가 글로벌 `@RestControllerAdvice`(JSON)로 새어 브라우저에 JSON이 뜨던 문제를,
web 전용 `WebControllerAdvice`(`@Order(HIGHEST_PRECEDENCE)`, basePackages=web)가 `ApiException`과
`MethodArgumentTypeMismatchException`을 가로채 HTML `error` 페이지로 렌더하도록 보강.
