package com.queueagent.service;

import com.queueagent.entity.DlqEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiIncidentAnalyzer {

    public String analyze(String errorType, Long count, List<DlqEvent> samples) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 OMS/WMS 재고 장애 분석가입니다.\n");
        sb.append("다음 DLQ 이벤트 그룹을 분석해 장애 리포트를 작성하세요.\n\n");

        sb.append("[장애 유형]\n");
        sb.append(errorType).append("\n\n");

        sb.append("[발생 건수]\n");
        sb.append(count).append("건\n\n");

        sb.append("[대표 샘플]\n");
        for (DlqEvent sample : samples) {
            sb.append("- productCode=").append(sample.getProductCode())
                    .append(", quantity=").append(sample.getQuantity())
                    .append(", errorMessage=").append(sample.getErrorMessage())
                    .append(", occurredAt=").append(sample.getCreatedAt())
                    .append("\n");
        }


        sb.append("\n다음 형식으로 답하세요:\n");
        sb.append("1. 장애 요약\n");
        sb.append("2. 추정 원인\n");
        sb.append("3. 영향 범위\n");
        sb.append("4. 운영 대응 방안\n");

        return sb.toString();
    }
}
