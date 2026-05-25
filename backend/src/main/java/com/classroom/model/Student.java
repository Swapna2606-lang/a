package com.classroom.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String email;

    private String seatPosition; // e.g., "R2C3" (Row 2, Col 3)

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus;

    @Column(columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double engagementScore;

    @Column(columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double attentionLevel;

    @Enumerated(EnumType.STRING)
    private EmotionState currentEmotion;

    private Boolean hasRaisedHand;
    private Boolean isParticipating;
    private Integer participationCount;

    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ClassSession session;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
        if (engagementScore == null) engagementScore = 0.0;
        if (attentionLevel == null) attentionLevel = 0.0;
        if (hasRaisedHand == null) hasRaisedHand = false;
        if (isParticipating == null) isParticipating = false;
        if (participationCount == null) participationCount = 0;
        if (currentEmotion == null) currentEmotion = EmotionState.NEUTRAL;
        if (attendanceStatus == null) attendanceStatus = AttendanceStatus.ABSENT;
    }

    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED
    }

    public enum EmotionState {
        ENGAGED, CONFUSED, BORED, HAPPY, NEUTRAL, ATTENTIVE, DISTRACTED
    }
}
