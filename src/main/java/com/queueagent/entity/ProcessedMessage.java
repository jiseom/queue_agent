package com.queueagent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "processed_messages")
public class ProcessedMessage { // 멱등성 체크용
    @Id
    private String idempotencyKey;

    private LocalDateTime processAt;

    public ProcessedMessage(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        this.processAt = LocalDateTime.now();
    }

}
