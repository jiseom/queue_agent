package com.queueagent.consumer;

import com.queueagent.entity.InventoryOutbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DlqConsumer {

    // 에러 정보는 InventoryConsumer 예외 시점에 이미 DB에 저장됨
    // DLQ 도착은 "재시도 소진 후 메시지 도달" 확인 용도
    @RabbitListener(queues = "inventory.dlq")
    public void consumeDlqMessage(InventoryOutbox message) {
        log.warn("DLQ 메시지 도착 - key={}, productCode={}, quantity={}",
                message.getIdempotencyKey(), message.getProductCode(), message.getQuantity());
    }
}
