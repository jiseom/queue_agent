package com.queueagent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_stock")
public class ProductStock {
    @Id
    private String productCode; // 상품 코드 (PK)

    @Column(nullable = false)
    private Integer initialQuantity; // 초기 재고 수량 (정합성 검증용)

    @Column(nullable = false)
    private Integer currentQuantity; // 현재 실제 재고 수량

    private LocalDateTime updatedAt;

    // 재고 변동 로직
    public void updateStock(int changeAmount) {
        this.currentQuantity += changeAmount;
        this.updatedAt = LocalDateTime.now();
    }
}
