package com.classroom.repository;

import com.classroom.model.EngagementEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EngagementEventRepository extends JpaRepository<EngagementEvent, Long> {
    List<EngagementEvent> findBySessionIdOrderByOccurredAtDesc(Long sessionId);
    List<EngagementEvent> findByStudentIdOrderByOccurredAtDesc(Long studentId);
    List<EngagementEvent> findTop50BySessionIdOrderByOccurredAtDesc(Long sessionId);
}
