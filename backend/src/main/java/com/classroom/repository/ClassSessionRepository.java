package com.classroom.repository;

import com.classroom.model.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    List<ClassSession> findByStatus(ClassSession.SessionStatus status);
    Optional<ClassSession> findByIdAndStatus(Long id, ClassSession.SessionStatus status);
    List<ClassSession> findByTeacherNameOrderByCreatedAtDesc(String teacherName);
}
