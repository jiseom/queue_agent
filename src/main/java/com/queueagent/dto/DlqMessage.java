package com.queueagent.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DlqMessage {
    private String productCode;
    private Integer quantity;
    private String idempotencyKey;
    private String errorType;
    private String errorMessage;

    private String originalQueue;
    private LocalDateTime failedAt;


}
