package com.classroom.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "engagement_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngagementEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Double engagementValue;
    private Double attentionValue;
    private String emotionDetected;
    private String details;
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {
        occurredAt = LocalDateTime.now();
    }

    public enum EventType {
        ATTENDANCE_MARKED,
        HAND_RAISED,
        QUESTION_ASKED,
        ANSWER_GIVEN,
        CONFUSION_DETECTED,
        DISTRACTION_DETECTED,
        ENGAGEMENT_UPDATE,
        EMOTION_CHANGE,
        VOICE_INTERACTION,
        PARTICIPATION_EVENT
    }
}
