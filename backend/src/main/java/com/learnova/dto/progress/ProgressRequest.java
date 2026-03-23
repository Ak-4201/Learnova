package com.learnova.dto.progress;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgressRequest {

    @NotNull
    private Long lessonId;

    private boolean completed;
}
