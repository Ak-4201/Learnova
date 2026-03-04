package com.learnova.repository;

import com.learnova.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.instructor ORDER BY c.createdAt DESC")
    List<Course> findAllWithInstructor();

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.instructor WHERE c.id = :id")
    java.util.Optional<Course> findByIdWithInstructor(Long id);
}
