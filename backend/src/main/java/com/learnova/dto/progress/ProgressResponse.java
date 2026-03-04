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
    private Long completedCount;
    private Long totalLessons;
    private Integer progressPercent;
}
