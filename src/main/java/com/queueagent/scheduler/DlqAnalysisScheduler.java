package com.queueagent.scheduler;

import com.queueagent.entity.DlqEvent;
import com.queueagent.enums.AnalysisStatus;
import com.queueagent.projection.DlqGroupCountProjection;
import com.queueagent.repository.DlqEventRepository;
import com.queueagent.service.AiIncidentAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DlqAnalysisScheduler {

    private final DlqEventRepository dlqEventRepository;
    private final AiIncidentAnalyzerService aiIncidentAnalyzerService;

    @Scheduled(fixedDelay = 300000) // 5분마다
    public void analyzeDlqEvents() {
        List<DlqGroupCountProjection> groups = dlqEventRepository.countGroupByErrorType();

        if (groups.isEmpty()) {
            log.info("분석할 DLQ 이벤트가 없습니다.");
            return;
        }

        for (DlqGroupCountProjection group : groups) {
            String errorType = group.getErrorType();
            Long count = group.getCnt();

            List<DlqEvent> samples =
                    dlqEventRepository.findTop10ByErrorTypeAndAnalysisStatusOrderByCreatedAtAsc(
                            errorType,
                            AnalysisStatus.PENDING
                    );

            if (samples.isEmpty()) {
                continue;
            }

            try {
                String report = aiIncidentAnalyzerService.analyze(errorType, count, samples);

                log.info("DLQ 분석 완료 - errorType={}, count={}\n{}",
                        errorType, count, report);

                dlqEventRepository.markAllByErrorTypeAsAnalyzed(
                        errorType,
                        LocalDateTime.now()
                );

            } catch (Exception e) {
                log.error("DLQ 분석 실패 - errorType={}", errorType, e);
            }
        }
    }
}