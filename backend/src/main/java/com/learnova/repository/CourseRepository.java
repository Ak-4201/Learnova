package com.learnova.repository;

import com.learnova.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOrderByIdAsc();

    Optional<Course> findByTitle(String title);

    Optional<Course> findFirstByTitleIgnoreCase(String title);

    @Query("SELECT c FROM Course c WHERE "
            + "(:search IS NULL OR :search = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "AND (:category IS NULL OR :category = '' OR c.category = :category) "
            + "ORDER BY c.id ASC")
    List<Course> findAllBySearchAndCategory(@Param("search") String search, @Param("category") String category);
}
