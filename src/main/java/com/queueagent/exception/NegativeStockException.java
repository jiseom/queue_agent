package com.queueagent.exception;

public class NegativeStockException extends RuntimeException {
    public NegativeStockException(String productCode, int changeAmount) {
        super(String.format("재고 부족 - 상품코드: %s, 변동수량: %d", productCode, changeAmount));
    }
}
