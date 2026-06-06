package com.jobboard.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatchRequest(
        @NotBlank(message = "Resume text is required")
        @Size(min = 50, max = 5000, message = "Resume must be 50-5000 characters")
        String resumeText
) {}