package com.learnova.dto.dashboard;

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
    private int progressPercent;
    private int completedLessons;
    private int totalLessons;
}
