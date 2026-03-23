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
    private List<LessonItemDto> lessons;
    private int totalLessons;
    private int progressPercent;
    private int completedCount;
    private Long lastWatchedLessonId;
    private Boolean enrolled;
}
