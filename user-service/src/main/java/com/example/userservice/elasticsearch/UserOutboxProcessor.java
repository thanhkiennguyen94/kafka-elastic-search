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
    private final OutboxRepository outboxRepository;       // dùng same Outbox table
    private final UserElasticRepository userElasticRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void processUserOutbox() {
        List<OutboxEvent> events = outboxRepository.findTop50ByStatusElasticOrderByIdAsc(OutboxStatus.PENDING);
        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            try {
                if (ConstantType.TYPE_USER.equals(event.getAggregateType())
                        && ConstantEventType.EVENT_CREATE_USER.equals(event.getEventType())) {

                    // Deserialize payload
                    CreateUserEvent payload = objectMapper.readValue(event.getPayload(), CreateUserEvent.class);

                    // Map sang UserDocument
                    UserDocument doc = UserDocument.builder()
                            .id(payload.getUserId())
                            .username(payload.getUsername())
                            .email(payload.getEmail())
                            .build();

                    // Save lên Elasticsearch
                    userElasticRepository.save(doc);

                    // Update status Outbox
                    event.setStatusElastic(OutboxStatus.SENT);
                    event.setRetryCount(event.getRetryCount() + 1);
                    event.setLastError(null);
                    outboxRepository.save(event);
                }
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= 3) {
                    event.setStatusElastic(OutboxStatus.FAILED);
                }
                outboxRepository.save(event);
            }
        }
    }
}
