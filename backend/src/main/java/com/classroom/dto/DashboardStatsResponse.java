package com.classroom.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsResponse {
    private Double averageEngagement;
    private Double averageAttention;
    private Integer presentStudents;
    private Integer totalStudents;
    private Integer activeParticipants;
    private Double teacherEffectivenessScore;
    private Map<String, Long> emotionDistribution;
    private List<StudentResponse> topEngagedStudents;
    private List<StudentResponse> atRiskStudents;
    private String sessionStatus;
    private Long sessionId;
}
