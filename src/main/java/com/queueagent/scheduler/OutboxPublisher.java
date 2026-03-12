package com.queueagent.scheduler;

import com.queueagent.config.RabbitConfig;
import com.queueagent.entity.InventoryOutbox;
import com.queueagent.repository.InventoryOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final InventoryOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendToMessageQueue(Long outboxId) {
        InventoryOutbox outbox = outboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, outbox);
        outbox.markAsSent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(Long outboxId, String errorMessage) {
        InventoryOutbox outbox = outboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));
        outbox.markAsFailed(errorMessage);
    }
}
