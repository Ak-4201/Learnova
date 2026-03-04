package com.learnova.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private String whatYouWillLearn;
    private String thumbnailUrl;
    private String category;
    private String instructorName;
    private Long instructorId;
    private Long totalLessons;
    private Long totalDurationSeconds;
    private Boolean enrolled;
}
