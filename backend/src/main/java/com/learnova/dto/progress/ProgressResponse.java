package com.learnova.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {

    private Long lessonId;
    private Boolean completed;
    private int completedCount;
    private int totalLessons;
    private int progressPercent;
}
