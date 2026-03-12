package com.queueagent.exception;

public class NegativeStockException extends RuntimeException {
    public NegativeStockException(String productCode, int currentQuantity, int changeAmount) {
        super(String.format("재고 부족 - 상품코드: %s, 현재재고: %d, 변동수량: %d", productCode, currentQuantity, changeAmount));
    }
}
