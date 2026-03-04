package com.learnova.repository;

import com.learnova.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l JOIN FETCH l.section s JOIN s.course c WHERE c.id = :courseId ORDER BY s.orderNumber, l.orderNumber")
    List<Lesson> findByCourseIdOrderBySectionAndOrder(Long courseId);

    Optional<Lesson> findById(Long id);

    @Query("SELECT COUNT(l) FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId")
    long countByCourseId(Long courseId);

    @Query("SELECT COALESCE(SUM(l.durationSeconds), 0L) FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId")
    Long sumDurationByCourseId(Long courseId);
}
