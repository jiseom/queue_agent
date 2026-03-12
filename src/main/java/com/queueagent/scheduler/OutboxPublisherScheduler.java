package com.queueagent.scheduler;

import com.queueagent.entity.InventoryOutbox;
import com.queueagent.enums.OutboxStatus;
import com.queueagent.repository.InventoryOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final InventoryOutboxRepository outboxRepository;
    private final OutboxPublisher outboxPublisher;

    /**
     * [Transactional Outbox Publisher]
     * DB에 저장된 PENDING 상태의 메시지를 1,000건씩 읽어 MQ로 확실히 전송합니다.
     */
//    @Scheduled(fixedDelay = 1000)
    public void publishPendingMessages() {
        Slice<InventoryOutbox> pendingMessages = outboxRepository.findByStatus(
                OutboxStatus.PENDING,
                PageRequest.of(0, 1000)
        );

        if (pendingMessages.isEmpty()) return;

        log.info("Outbox 발행 시작: {}건", pendingMessages.getNumberOfElements());

        for (InventoryOutbox outbox : pendingMessages) {
            try {
                outboxPublisher.sendToMessageQueue(outbox.getId());
            } catch (Exception e) {
                log.error("MQ 발행 실패 - ID: {}, 사유: {}", outbox.getId(), e.getMessage(), e);
                outboxPublisher.markAsFailed(outbox.getId(), e.getMessage());
            }
        }
    }
}
