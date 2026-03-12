package com.queueagent.service;

import com.queueagent.entity.InventoryOutbox;
import com.queueagent.entity.ProcessedMessage;
import com.queueagent.entity.ProductStock;
import com.queueagent.exception.NegativeStockException;
import com.queueagent.repository.OmsInventoryRepository;
import com.queueagent.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 2. 멱등성 체크
        if (processedMessageRepository.existsById(message.getIdempotencyKey())) {
            log.info("이미 처리된 중복 메시지입니다. 키: {}", message.getIdempotencyKey());
            return;
        }

        // 3. 상품 조회
        ProductStock stock = omsInventoryRepository.findById(message.getProductCode())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품코드입니다: " + message.getProductCode()));

        // 4. 음수 재고 검증
        int finalQuantity = stock.getCurrentQuantity() + message.getQuantity();
        if (finalQuantity < 0) {
            throw new NegativeStockException(message.getProductCode(), stock.getCurrentQuantity(), message.getQuantity());
        }

        // 5. 재고 업데이트
        stock.updateStock(message.getQuantity());

        // 6. 멱등성 기록 저장
        try {
            processedMessageRepository.save(new ProcessedMessage(message.getIdempotencyKey()));
        } catch (DataIntegrityViolationException e) {
            log.info("동시 중복 메시지 감지, 무시합니다. 키: {}", message.getIdempotencyKey());
            return;
        }

        log.info("재고 업데이트 완료: 상품코드={}, 변동수량={}, 최종재고={}", message.getProductCode(), message.getQuantity(), finalQuantity);
    }
}
