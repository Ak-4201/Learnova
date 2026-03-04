package com.learnova.repository;

import com.learnova.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserIdAndLessonId(Long userId, Long lessonId);

    @Query("SELECT p FROM Progress p WHERE p.user.id = :userId AND p.lesson.id IN (SELECT l.id FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId)")
    List<Progress> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT p FROM Progress p WHERE p.user.id = :userId AND p.lesson.section.course.id = :courseId AND p.completed = true")
    List<Progress> findCompletedByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT p FROM Progress p WHERE p.user.id = :userId AND p.lesson.section.course.id = :courseId ORDER BY p.lastWatchedAt DESC")
    List<Progress> findLastWatchedByUserIdAndCourseId(Long userId, Long courseId);
}
