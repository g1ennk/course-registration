package hello.courseregistration.enrollment.service;

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
    void 정원이_1인_강의에_여러명이_동시신청해도_정확히_1건만_성공한다() throws Exception {
        // given: capacity = 1인 OPEN 상태인 강의
        Course course = new Course(1L, "강의", "설명", 10000, 1,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        course.changeStatusTo(CourseStatus.OPEN);
        Long courseId = courseRepository.save(course).getId();

        // set-up
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1); // 발사신호
        CountDownLatch done = new CountDownLatch(threadCount); // 완료 대기
        AtomicInteger success = new AtomicInteger();

        // when: 서로 다른 학생 10명이 동시에 신청
        for (int i = 0; i < threadCount; i++) {
            long classmateId = 100 + i; // 학생 ID는 모두 다르게
            executor.submit(() -> {
                try {
                    start.await(); // 출발선 대기
                    enrollmentService.apply(courseId, classmateId);
                    success.incrementAndGet();
                } catch (Exception ignored) {
                    // 정원 초과 신청자는 무시
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown(); // 동시 출발
        done.await(); // 전원 종료까지 대기
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then: 성공은 정확히 1건, DB 활성 신청도 정확히 1건
        assertThat(success.get()).isEqualTo(1);
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(courseId, EnrollmentStatus.ACTIVE)).isEqualTo(1);
    }

}
