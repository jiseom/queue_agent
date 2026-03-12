package com.queueagent.service;

import com.queueagent.entity.InventoryOutbox;
import com.queueagent.entity.TempStockRequest;
import com.queueagent.repository.InventoryOutboxRepository;
import com.queueagent.repository.TempStockRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private final InventoryOutboxRepository outboxRepository;
    private final TempStockRequestRepository tempStockRequestRepository;

    /**
     * 프로듀서
     * WMS 재고 변동 이벤트를 Outbox에 PENDING 상태로 저장합니다.
     * 실제 MQ 발행은 OutboxPublisherScheduler가 담당합니다.
     */
    @Transactional
    public void processStockChange(String productCode, Integer quantity) {
        outboxRepository.save(InventoryOutbox.builder()
                .productCode(productCode)
                .quantity(quantity)
                .idempotencyKey(UUID.randomUUID().toString())
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleRequest(TempStockRequest request) {
        outboxRepository.save(InventoryOutbox.builder()
                .productCode(request.getProductCode())
                .quantity(request.getQuantity())
                .idempotencyKey("STOCK_REQ_" + request.getId())
                .build());

        request.markAsProcessed();
        tempStockRequestRepository.save(request);
    }
}
