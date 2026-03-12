package com.queueagent.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "temp_stock_request")
public class TempStockRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCode;
    private Integer quantity;

    @Column(nullable = false)
    private Boolean processed = false; // 처리 완료 여부 (실패 지점 재처리를 위한 핵심 필드)

    private LocalDateTime createdAt;

    // 처리 완료 상태 변경 메서드
    public void markAsProcessed() {
        this.processed = true;
    }
}

