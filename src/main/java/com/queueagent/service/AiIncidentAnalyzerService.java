package com.queueagent.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.queueagent.entity.DlqEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiIncidentAnalyzerService {

    private final AnthropicClient anthropicClient;
    private final AiIncidentAnalyzer aiIncidentAnalyzer;

    public String analyze(String errorType, Long count, List<DlqEvent> samples) {
        String prompt = aiIncidentAnalyzer.analyze(errorType, count, samples);

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_OPUS_4_6)
                .maxTokens(4096L)
                .thinking(ThinkingConfigAdaptive.builder().build())
                .addUserMessage(prompt)
                .build();

        try (StreamResponse<RawMessageStreamEvent> stream = anthropicClient.messages().createStreaming(params)) {
            return stream.stream()
                    .flatMap(event -> event.contentBlockDelta().stream())
                    .flatMap(e -> e.delta().text().stream())
                    .map(t -> t.text())
                    .collect(Collectors.joining());
        }
    }
}
