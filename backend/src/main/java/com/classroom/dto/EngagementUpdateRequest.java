package com.classroom.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EngagementUpdateRequest {
    private Long studentId;
    private Long sessionId;
    private Double engagementScore;
    private Double attentionLevel;
    private String emotion;
    private String eventType;
    private String details;
}
