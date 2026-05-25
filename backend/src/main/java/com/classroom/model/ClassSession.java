package com.classroom.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionName;

    private String subject;
    private String teacherName;
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double averageEngagement;

    @Column(columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double teacherEffectivenessScore;

    private Integer totalStudents;
    private Integer presentStudents;
    private Integer activeParticipants;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EngagementEvent> events = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = SessionStatus.SCHEDULED;
        if (averageEngagement == null) averageEngagement = 0.0;
        if (teacherEffectivenessScore == null) teacherEffectivenessScore = 0.0;
        if (totalStudents == null) totalStudents = 0;
        if (presentStudents == null) presentStudents = 0;
        if (activeParticipants == null) activeParticipants = 0;
    }

    public enum SessionStatus {
        SCHEDULED, ACTIVE, PAUSED, COMPLETED
    }
}
