package hello.courseregistration.enrollment.service;

import hello.courseregistration.common.exception.ApiException;
import hello.courseregistration.common.exception.ErrorCode;
import hello.courseregistration.course.domain.Course;
import hello.courseregistration.course.domain.CourseStatus;
import hello.courseregistration.course.repository.CourseRepository;
import hello.courseregistration.enrollment.domain.EnrollmentStatus;
import hello.courseregistration.enrollment.repository.EnrollmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EnrollmentConcurrencyTest {

    @Autowired
    EnrollmentService enrollmentService;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @AfterEach
    void tearDown() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void 정원이_1인_강의에_10명이_동시신청해도_정확히_1건만_성공한다() throws Exception {
        Long courseId = openCourse(1);

        Result result = applyConcurrently(courseId, 10);

        // 예기치 못한 예외(락 타임아웃·데드락 등)는 없어야 한다
        assertThat(result.unexpected).isEmpty();
        // 성공은 정확히 1건, DB 활성 신청도 정확히 1건
        assertThat(result.success).isEqualTo(1);
        assertThat(activeCount(courseId)).isEqualTo(1);
    }

    @Test
    void 정원이_5인_강의에_50명이_동시신청하면_정확히_5건만_성공한다() throws Exception {
        Long courseId = openCourse(5);

        Result result = applyConcurrently(courseId, 50);

        assertThat(result.unexpected).isEmpty();
        // capacity=1은 'first-wins' 잘못된 구현도 통과하므로, N>1로 카운트 로직까지 검증한다
        assertThat(result.success).isEqualTo(5);
        assertThat(activeCount(courseId)).isEqualTo(5);
    }

    // 서로 다른 학생 threadCount명이 동시에 신청하도록 실행하고 결과를 모은다
    private Result applyConcurrently(Long courseId, int threadCount) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);         // 발사 신호
        CountDownLatch done = new CountDownLatch(threadCount); // 완료 대기
        AtomicInteger success = new AtomicInteger();
        List<Throwable> unexpected = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            long classmateId = 100 + i; // 학생 ID는 모두 다르게 (정원 경쟁만 분리)
            executor.submit(() -> {
                try {
                    start.await();
                    enrollmentService.apply(courseId, classmateId);
                    success.incrementAndGet();
                } catch (ApiException e) {
                    // 만석(정원 초과)만 기대되는 거부 — 그 외 ApiException은 비정상
                    if (e.getErrorCode() != ErrorCode.COURSE_FULL) {
                        unexpected.add(e);
                    }
                } catch (Throwable t) {
                    unexpected.add(t); // 락 타임아웃·데드락 등은 테스트를 빨갛게
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown(); // 동시 출발
        boolean finished = done.await(10, TimeUnit.SECONDS); // 타임아웃으로 무한 hang 방지
        executor.shutdownNow();
        assertThat(finished).as("모든 스레드가 10초 내 완료되어야 한다").isTrue();

        return new Result(success.get(), unexpected);
    }

    private Long openCourse(int capacity) {
        Course course = new Course(1L, "강의", "설명", 10000, capacity,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        course.changeStatusTo(CourseStatus.OPEN);
        return courseRepository.save(course).getId();
    }

    private long activeCount(Long courseId) {
        return enrollmentRepository.countByCourseIdAndStatusIn(courseId, EnrollmentStatus.ACTIVE);
    }

    private record Result(int success, List<Throwable> unexpected) {
    }
}
