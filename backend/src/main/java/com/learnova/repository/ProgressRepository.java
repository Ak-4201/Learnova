package com.learnova.repository;

import com.learnova.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    List<Progress> findByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Progress> findByUserIdAndCourseIdAndLessonId(Long userId, Long courseId, Long lessonId);

    long countByUserIdAndCourseIdAndCompletedTrue(Long userId, Long courseId);

    Optional<Progress> findTop1ByUserIdAndCourseIdOrderByLastWatchedAtDesc(Long userId, Long courseId);
}
