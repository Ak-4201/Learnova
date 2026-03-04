package com.learnova.dto.progress;

import lombok.Data;

@Data
public class ProgressRequest {
    private Long lessonId;
    private Boolean completed;
}
