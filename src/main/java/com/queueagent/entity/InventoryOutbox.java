package com.queueagent.entity;

import com.queueagent.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name="inventory_outbox")
public class InventoryOutbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private String errorMessage;

    @Builder
    public InventoryOutbox(String productCode, Integer quantity, String idempotencyKey) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.idempotencyKey = idempotencyKey;
        this.status = OutboxStatus.PENDING;
    }

    public void markAsSent() {
        this.status = OutboxStatus.SENT;
    }

    public void markAsFailed(String message){
        this.status = OutboxStatus.FAILED;
        this.errorMessage = message;
    }


}
