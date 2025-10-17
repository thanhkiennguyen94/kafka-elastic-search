package com.example.userservice.elasticsearch;

import com.example.commonservice.event.CreateUserEvent;
import com.example.commonservice.outbox.OutboxEvent;
import com.example.commonservice.outbox.OutboxRepository;
import com.example.commonservice.outbox.OutboxStatus;
import com.example.commonservice.util.ConstantEventType;
import com.example.commonservice.util.ConstantType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserOutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final UserElasticRepository userElasticRepository;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRY = 3;

    // xử lý theo schedule
    @Scheduled(fixedDelay = 5000)
    public void processUserOutbox() {
        List<OutboxEvent> events = outboxRepository.findTop50ByStatusElasticOrderByIdAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : events) {
            processSingleEvent(event);
        }
    }

    public void processSingleEvent(OutboxEvent event) {
        if (!ConstantType.TYPE_USER.equals(event.getAggregateType()) ||
                !ConstantEventType.EVENT_CREATE_USER.equals(event.getEventType())) {
            return;
        }

        try {
            CreateUserEvent payload = objectMapper.readValue(event.getPayload(), CreateUserEvent.class);
            UserDocument doc = UserDocument.builder()
                    .id(payload.getUserId())
                    .username(payload.getUsername())
                    .email(payload.getEmail())
                    .build();
            userElasticRepository.save(doc);

            event.setStatusElastic(OutboxStatus.SENT);
            event.setLastError(null);
            outboxRepository.save(event);
        } catch (Exception e) {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(e.getMessage());
            if (event.getRetryCount() >= MAX_RETRY) {
                event.setStatusElastic(OutboxStatus.FAILED);
            }
            outboxRepository.save(event);
        }
    }
}
