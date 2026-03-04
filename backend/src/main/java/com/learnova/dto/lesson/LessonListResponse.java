package com.learnova.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonListResponse {
    private Long courseId;
    private List<LessonDto> lessons;
    private Long totalLessons;
    private Long completedCount;
    private Integer progressPercent;
    private Long lastWatchedLessonId;
    private Boolean enrolled;
}
