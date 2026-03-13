package com.queueagent.repository;

import com.queueagent.entity.DlqEvent;
import com.queueagent.enums.AnalysisStatus;
import com.queueagent.projection.DlqGroupCountProjection;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DlqEventRepository extends JpaRepository<DlqEvent, Long> {

    List<DlqEvent> findTop2500ByAnalysisStatusOrderByCreatedAtAsc(AnalysisStatus status);

    @Query("""
        select d.errorType as errorType, count(d) as cnt
        from DlqEvent d
        where d.analysisStatus = 'PENDING'
        group by d.errorType
        order by count(d) desc
    """)
    List<DlqGroupCountProjection> countGroupByErrorType();

    List<DlqEvent> findTop10ByErrorTypeAndAnalysisStatusOrderByCreatedAtAsc(
            String errorType,
            AnalysisStatus status
    );

    @Modifying
    @Transactional
    @Query("""
                update DlqEvent d
                set d.analysisStatus = com.queueagent.enums.AnalysisStatus.ANALYZED,
                    d.analyzedAt = :analyzedAt
                where d.errorType = :errorType
                  and d.analysisStatus = com.queueagent.enums.AnalysisStatus.PENDING
            """)
    int markAllByErrorTypeAsAnalyzed(
            @Param("errorType") String errorType,
            @Param("analyzedAt") LocalDateTime analyzedAt
    );
}
