package com.queueagent.consumer;

import com.queueagent.config.RabbitConfig;
import com.queueagent.entity.InventoryOutbox;
import com.queueagent.service.InventoryConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryConsumerService inventoryConsumerService;

    /**
     * RabbitMQ의 'inventory.queue'로부터 메시지를 구독(Subscribe)합니다.
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(InventoryOutbox message) {
        try {
            // 전달받은 메시지를 처리 서비스로 넘깁니다.
            inventoryConsumerService.processInventoryUpdate(message);
        } catch (Exception e) {
            // 예외 발생 시 로그를 남기고, 설정된 DLQ로 메시지가 이동되도록 유도합니다.
            // 이 실패 로그는 나중에 AI 에이전트가 분석할 중요한 데이터가 됩니다
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e; // 메시지 재시도 또는 DLQ 이동을 위해 예외를 다시 던집니다.
        }
    }
}
