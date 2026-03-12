package com.queueagent.service;

import com.queueagent.entity.InventoryOutbox;
import com.queueagent.entity.ProcessedMessage;
import com.queueagent.exception.NegativeStockException;
import com.queueagent.repository.OmsInventoryRepository;
import com.queueagent.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryConsumerService {
    private final ProcessedMessageRepository processedMessageRepository;
    private final OmsInventoryRepository omsInventoryRepository;

    @Transactional
    public void processInventoryUpdate(InventoryOutbox message) {
        // 1. 의미 없는 이벤트 조기 차단
        if (message.getQuantity() == 0) {
            log.info("수량이 0인 이벤트는 처리하지 않습니다. 키: {}", message.getIdempotencyKey());
            return;
        }

        // 2. 멱등성 선점 (unique 제약이 최종 방어선)
        if (processedMessageRepository.existsById(message.getIdempotencyKey())) {
            log.info("이미 처리된 중복 메시지입니다. 키: {}", message.getIdempotencyKey());
            return;
        }

        try {
            processedMessageRepository.saveAndFlush(new ProcessedMessage(message.getIdempotencyKey()));
        } catch (DataIntegrityViolationException e) {
            log.info("동시 중복 메시지 감지, 정상 종료합니다. 키: {}", message.getIdempotencyKey());
            return;
        }

        // 3. 원자적 재고 업데이트 (currentQuantity + quantity >= 0 조건 포함)
        int updated = omsInventoryRepository.updateQuantity(
                message.getProductCode(), message.getQuantity(), LocalDateTime.now());

        if (updated == 0) {
            if (!omsInventoryRepository.existsById(message.getProductCode())) {
                throw new IllegalArgumentException("존재하지 않는 상품코드입니다: " + message.getProductCode());
            }
            throw new NegativeStockException(message.getProductCode(), message.getQuantity());
        }

        log.info("재고 업데이트 완료: 상품코드={}, 변동수량={}", message.getProductCode(), message.getQuantity());
    }
}
