package com.learnova.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledCourseDto {
    private Long courseId;
    private String title;
    private String thumbnailUrl;
    private Integer progressPercent;
    private Long completedLessons;
    private Long totalLessons;
    private Long lastWatchedLessonId;
}
