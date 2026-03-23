package com.learnova.service;

import com.learnova.entity.Enrollment;
import com.learnova.repository.CourseRepository;
import com.learnova.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public void enroll(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            return; // already enrolled
        }
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found");
        }
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .enrolledAt(Instant.now())
                .build();
        enrollmentRepository.save(enrollment);
    }
}
