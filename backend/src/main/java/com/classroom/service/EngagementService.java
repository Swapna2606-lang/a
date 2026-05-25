package com.classroom.service;

import com.classroom.dto.*;
import com.classroom.model.*;
import com.classroom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EngagementService {

    private final StudentRepository studentRepository;
    private final ClassSessionRepository sessionRepository;
    private final EngagementEventRepository eventRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();

    // ==================== Session Management ====================

    public ClassSession createSession(SessionCreateRequest request) {
        ClassSession session = ClassSession.builder()
                .sessionName(request.getSessionName())
                .subject(request.getSubject())
                .teacherName(request.getTeacherName())
                .roomNumber(request.getRoomNumber())
                .status(ClassSession.SessionStatus.SCHEDULED)
                .build();
        return sessionRepository.save(session);
    }

    public ClassSession startSession(Long sessionId) {
        ClassSession session = getSessionOrThrow(sessionId);
        session.setStatus(ClassSession.SessionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        session = sessionRepository.save(session);
        broadcastSessionUpdate(session);
        log.info("Session {} started", sessionId);
        return session;
    }

    public ClassSession endSession(Long sessionId) {
        ClassSession session = getSessionOrThrow(sessionId);
        session.setStatus(ClassSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public List<ClassSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    public ClassSession getSession(Long id) {
        return getSessionOrThrow(id);
    }

    // ==================== Student Management ====================

    public Student addStudent(StudentCreateRequest request) {
        ClassSession session = getSessionOrThrow(request.getSessionId());

        Student student = Student.builder()
                .name(request.getName())
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .seatPosition(request.getSeatPosition())
                .session(session)
                .attendanceStatus(Student.AttendanceStatus.ABSENT)
                .build();

        student = studentRepository.save(student);
        session.setTotalStudents(session.getStudents().size() + 1);
        sessionRepository.save(session);
        return student;
    }

    public Student markAttendance(Long studentId, String status) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        student.setAttendanceStatus(Student.AttendanceStatus.valueOf(status));
        student.setLastActiveAt(LocalDateTime.now());

        if (status.equals("PRESENT")) {
            student.setEngagementScore(60.0 + random.nextDouble() * 20);
            student.setAttentionLevel(65.0 + random.nextDouble() * 20);
            student.setCurrentEmotion(Student.EmotionState.NEUTRAL);
        }

        student = studentRepository.save(student);

        // Log event
        logEvent(student.getSession(), student,
                EngagementEvent.EventType.ATTENDANCE_MARKED,
                student.getEngagementScore(), student.getAttentionLevel(),
                status, "Attendance: " + status);

        updateSessionStats(student.getSession().getId());
        broadcastStudentUpdate(student);
        return student;
    }

    public List<Student> getStudentsBySession(Long sessionId) {
        return studentRepository.findBySessionId(sessionId);
    }

    // ==================== Engagement Tracking ====================

    public Student updateEngagement(EngagementUpdateRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (request.getEngagementScore() != null) {
            student.setEngagementScore(clamp(request.getEngagementScore(), 0, 100));
        }
        if (request.getAttentionLevel() != null) {
            student.setAttentionLevel(clamp(request.getAttentionLevel(), 0, 100));
        }
        if (request.getEmotion() != null) {
            student.setCurrentEmotion(Student.EmotionState.valueOf(request.getEmotion()));
        }
        student.setLastActiveAt(LocalDateTime.now());
        student = studentRepository.save(student);

        EngagementEvent.EventType eventType = EngagementEvent.EventType.ENGAGEMENT_UPDATE;
        if (request.getEventType() != null) {
            try { eventType = EngagementEvent.EventType.valueOf(request.getEventType()); } catch (Exception ignored) {}
        }

        logEvent(student.getSession(), student, eventType,
                student.getEngagementScore(), student.getAttentionLevel(),
                request.getEmotion(), request.getDetails());

        updateSessionStats(student.getSession().getId());
        broadcastStudentUpdate(student);
        return student;
    }

    public Student raiseHand(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        student.setHasRaisedHand(true);
        student.setIsParticipating(true);
        student.setParticipationCount(student.getParticipationCount() + 1);
        student.setEngagementScore(Math.min(100, student.getEngagementScore() + 10));
        student.setLastActiveAt(LocalDateTime.now());
        student = studentRepository.save(student);

        logEvent(student.getSession(), student, EngagementEvent.EventType.HAND_RAISED,
                student.getEngagementScore(), student.getAttentionLevel(), null, "Hand raised");

        broadcastStudentUpdate(student);
        return student;
    }

    // ==================== Dashboard & Analytics ====================

    public DashboardStatsResponse getDashboardStats(Long sessionId) {
        ClassSession session = getSessionOrThrow(sessionId);
        List<Student> students = studentRepository.findBySessionId(sessionId);
        List<Student> presentStudents = students.stream()
                .filter(s -> s.getAttendanceStatus() == Student.AttendanceStatus.PRESENT)
                .collect(Collectors.toList());

        double avgEngagement = presentStudents.stream()
                .mapToDouble(s -> s.getEngagementScore() != null ? s.getEngagementScore() : 0)
                .average().orElse(0);

        double avgAttention = presentStudents.stream()
                .mapToDouble(s -> s.getAttentionLevel() != null ? s.getAttentionLevel() : 0)
                .average().orElse(0);

        int activeParticipants = (int) presentStudents.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsParticipating())).count();

        Map<String, Long> emotionDist = presentStudents.stream()
                .filter(s -> s.getCurrentEmotion() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getCurrentEmotion().name(),
                        Collectors.counting()
                ));

        List<StudentResponse> topEngaged = presentStudents.stream()
                .sorted(Comparator.comparingDouble(s -> -(s.getEngagementScore() != null ? s.getEngagementScore() : 0)))
                .limit(5)
                .map(this::toStudentResponse)
                .collect(Collectors.toList());

        List<StudentResponse> atRisk = presentStudents.stream()
                .filter(s -> s.getEngagementScore() != null && s.getEngagementScore() < 40)
                .map(this::toStudentResponse)
                .collect(Collectors.toList());

        double teacherScore = calculateTeacherEffectiveness(avgEngagement, avgAttention, activeParticipants, presentStudents.size());

        return DashboardStatsResponse.builder()
                .averageEngagement(Math.round(avgEngagement * 10.0) / 10.0)
                .averageAttention(Math.round(avgAttention * 10.0) / 10.0)
                .presentStudents(presentStudents.size())
                .totalStudents(students.size())
                .activeParticipants(activeParticipants)
                .teacherEffectivenessScore(Math.round(teacherScore * 10.0) / 10.0)
                .emotionDistribution(emotionDist)
                .topEngagedStudents(topEngaged)
                .atRiskStudents(atRisk)
                .sessionStatus(session.getStatus().name())
                .sessionId(sessionId)
                .build();
    }

    // ==================== AI Simulation (for demo) ====================

    @Scheduled(fixedDelay = 5000)
    public void simulateEngagementUpdates() {
        List<ClassSession> activeSessions = sessionRepository.findByStatus(ClassSession.SessionStatus.ACTIVE);
        for (ClassSession session : activeSessions) {
            List<Student> presentStudents = studentRepository.findPresentStudentsBySessionId(session.getId());
            for (Student student : presentStudents) {
                simulateStudentEngagement(student);
            }
            updateSessionStats(session.getId());
        }
    }

    private void simulateStudentEngagement(Student student) {
        // Simulate realistic engagement fluctuation
        double currentEngagement = student.getEngagementScore() != null ? student.getEngagementScore() : 70;
        double delta = (random.nextDouble() - 0.45) * 8; // slight positive bias
        double newEngagement = clamp(currentEngagement + delta, 10, 100);

        double currentAttention = student.getAttentionLevel() != null ? student.getAttentionLevel() : 70;
        double attDelta = (random.nextDouble() - 0.45) * 6;
        double newAttention = clamp(currentAttention + attDelta, 10, 100);

        Student.EmotionState emotion = simulateEmotion(newEngagement, newAttention);

        student.setEngagementScore(Math.round(newEngagement * 10.0) / 10.0);
        student.setAttentionLevel(Math.round(newAttention * 10.0) / 10.0);
        student.setCurrentEmotion(emotion);
        student.setLastActiveAt(LocalDateTime.now());

        // Random hand raise
        if (random.nextDouble() < 0.02) {
            student.setHasRaisedHand(true);
            student.setIsParticipating(true);
            student.setParticipationCount(student.getParticipationCount() + 1);
        } else {
            student.setHasRaisedHand(false);
        }

        studentRepository.save(student);
        broadcastStudentUpdate(student);
    }

    private Student.EmotionState simulateEmotion(double engagement, double attention) {
        if (engagement > 80 && attention > 80) return Student.EmotionState.ENGAGED;
        if (engagement > 70) return Student.EmotionState.ATTENTIVE;
        if (engagement < 30 || attention < 30) return Student.EmotionState.DISTRACTED;
        if (attention < 45) return Student.EmotionState.BORED;
        if (random.nextDouble() < 0.1) return Student.EmotionState.CONFUSED;
        return Student.EmotionState.NEUTRAL;
    }

    // ==================== Helpers ====================

    private void updateSessionStats(Long sessionId) {
        ClassSession session = getSessionOrThrow(sessionId);
        List<Student> present = studentRepository.findPresentStudentsBySessionId(sessionId);

        double avgEng = present.stream()
                .mapToDouble(s -> s.getEngagementScore() != null ? s.getEngagementScore() : 0)
                .average().orElse(0);

        double avgAtt = present.stream()
                .mapToDouble(s -> s.getAttentionLevel() != null ? s.getAttentionLevel() : 0)
                .average().orElse(0);

        int active = (int) present.stream().filter(s -> Boolean.TRUE.equals(s.getIsParticipating())).count();

        session.setAverageEngagement(Math.round(avgEng * 10.0) / 10.0);
        session.setPresentStudents(present.size());
        session.setActiveParticipants(active);
        session.setTeacherEffectivenessScore(calculateTeacherEffectiveness(avgEng, avgAtt, active, present.size()));
        sessionRepository.save(session);
    }

    private double calculateTeacherEffectiveness(double avgEngagement, double avgAttention, int activeParticipants, int totalPresent) {
        double participationRate = totalPresent > 0 ? (double) activeParticipants / totalPresent * 100 : 0;
        return (avgEngagement * 0.4 + avgAttention * 0.4 + participationRate * 0.2);
    }

    private void logEvent(ClassSession session, Student student, EngagementEvent.EventType type,
                          Double engagement, Double attention, String emotion, String details) {
        EngagementEvent event = EngagementEvent.builder()
                .session(session)
                .student(student)
                .eventType(type)
                .engagementValue(engagement)
                .attentionValue(attention)
                .emotionDetected(emotion)
                .details(details)
                .build();
        eventRepository.save(event);
    }

    private void broadcastStudentUpdate(Student student) {
        StudentResponse response = toStudentResponse(student);
        messagingTemplate.convertAndSend(
                "/topic/session/" + student.getSession().getId() + "/students",
                Map.of("type", "STUDENT_UPDATE", "data", response)
        );
    }

    private void broadcastSessionUpdate(ClassSession session) {
        messagingTemplate.convertAndSend(
                "/topic/session/" + session.getId() + "/status",
                Map.of("type", "SESSION_UPDATE", "status", session.getStatus())
        );
    }

    private ClassSession getSessionOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found: " + id));
    }

    private StudentResponse toStudentResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .studentId(s.getStudentId())
                .email(s.getEmail())
                .seatPosition(s.getSeatPosition())
                .attendanceStatus(s.getAttendanceStatus() != null ? s.getAttendanceStatus().name() : null)
                .engagementScore(s.getEngagementScore())
                .attentionLevel(s.getAttentionLevel())
                .currentEmotion(s.getCurrentEmotion() != null ? s.getCurrentEmotion().name() : null)
                .hasRaisedHand(s.getHasRaisedHand())
                .isParticipating(s.getIsParticipating())
                .participationCount(s.getParticipationCount())
                .lastActiveAt(s.getLastActiveAt())
                .build();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
