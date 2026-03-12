package com.queueagent.entity;

import com.queueagent.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dlq_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DlqEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCode;

    private Integer quantity;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String errorType;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String originalPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus analysisStatus;

    private LocalDateTime createdAt;

    private LocalDateTime analyzedAt;

    public void markAnalyzed() {
        this.analysisStatus = AnalysisStatus.ANALYZED;
        this.analyzedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.analysisStatus == null) {
            this.analysisStatus = AnalysisStatus.PENDING;
        }
    }
}
