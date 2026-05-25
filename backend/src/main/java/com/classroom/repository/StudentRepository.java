package com.classroom.repository;

import com.classroom.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    List<Student> findBySessionId(Long sessionId);

    @Query("SELECT AVG(s.engagementScore) FROM Student s WHERE s.session.id = :sessionId")
    Double findAverageEngagementBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT s FROM Student s WHERE s.session.id = :sessionId AND s.attendanceStatus = 'PRESENT'")
    List<Student> findPresentStudentsBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.session.id = :sessionId AND s.attendanceStatus = 'PRESENT'")
    Long countPresentStudentsBySessionId(@Param("sessionId") Long sessionId);
}
