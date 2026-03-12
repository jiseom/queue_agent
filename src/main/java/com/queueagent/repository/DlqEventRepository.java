package com.queueagent.repository;

import com.queueagent.entity.DlqEvent;
import com.queueagent.enums.AnalysisStatus;
import com.queueagent.projection.DlqGroupCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
