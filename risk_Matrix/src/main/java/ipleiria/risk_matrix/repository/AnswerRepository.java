package ipleiria.risk_matrix.repository;
import ipleiria.risk_matrix.models.answers.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {


    List<Answer> findByEmail(String email);

    List<Answer> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);


    List<Answer> findBySubmissionId(String submissionId);

    @Query("""
            SELECT a.submissionId
            FROM Answer a
            GROUP BY a.submissionId
            ORDER BY MAX(a.createdAt) DESC
            """)
    Page<String> findSubmissionIdsOrderByLatestAnswer(Pageable pageable);

    @Query("""
            SELECT a.submissionId
            FROM Answer a
            WHERE a.createdAt BETWEEN :start AND :end
            GROUP BY a.submissionId
            ORDER BY MAX(a.createdAt) DESC
            """)
    Page<String> findSubmissionIdsByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    List<Answer> findBySubmissionIdIn(List<String> submissionIds);

    void deleteBySubmissionId(String submissionId);
}
