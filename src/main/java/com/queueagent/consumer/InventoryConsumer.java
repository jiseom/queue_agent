package com.queueagent.consumer;

import com.queueagent.config.RabbitConfig;
import com.queueagent.entity.DlqEvent;
import com.queueagent.entity.InventoryOutbox;
import com.queueagent.repository.DlqEventRepository;
import com.queueagent.service.InventoryConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryConsumerService inventoryConsumerService;
    private final DlqEventRepository dlqEventRepository;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(InventoryOutbox message) {
        try {
            inventoryConsumerService.processInventoryUpdate(message);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            saveErrorToDlqEvent(message, e);
            throw e;
        }
    }

    private void saveErrorToDlqEvent(InventoryOutbox message, Exception e) {
        try {
            dlqEventRepository.save(DlqEvent.builder()
                    .productCode(message.getProductCode())
                    .quantity(message.getQuantity())
                    .idempotencyKey(message.getIdempotencyKey())
                    .errorType(e.getClass().getSimpleName())
                    .errorMessage(e.getMessage())
                    .build());
        } catch (DataIntegrityViolationException ignored) {
            // 재시도 중 중복 저장 무시 (idempotencyKey unique 제약)
        }
    }
}
