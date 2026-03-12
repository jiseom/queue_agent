package com.queueagent.scheduler;

import com.queueagent.entity.TempStockRequest;
import com.queueagent.repository.TempStockRequestRepository;
import com.queueagent.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockRequestScheduler {

    private final TempStockRequestRepository tempRepository;
    private final InventoryService inventoryService; // 실제 비즈니스 로직 담당

    @Scheduled(fixedDelay = 1000) // 1초 간격 실행
    public void processStockRequests() {
        // 1. 1,000건 단위 청크 조회
        PageRequest pageRequest = PageRequest.of(0, 1000);
        Slice<TempStockRequest> requestSlice = tempRepository.findByProcessedFalse(pageRequest);

        if (requestSlice.isEmpty()) {
            return;
        }

        log.info("청크 처리 시작: {}건", requestSlice.getNumberOfElements());

        // 2. 각 요청을 독립적인 트랜잭션으로 처리하여 부분 실패 허용
        for (TempStockRequest request : requestSlice) {
            try {
                // 개별 요청 처리 (InventoryService 내부에서 @Transactional(REQUIRES_NEW) 사용)
                inventoryService.processSingleRequest(request);
            } catch (Exception e) {
                // 실패 시 상세 에러 로그 기록 (AI 에이전트 분석용 데이터 확보)
                log.error("처리 실패 - ID: {}, 사유: {}", request.getId(), e.getMessage());
                // 필요 시 Slack 알림 연동
            }
        }
    }
}