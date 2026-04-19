package ru.practicum.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.UserActionUpdateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final UserActionUpdateService userActionUpdateService;

    @KafkaListener(
            topics = "${analyzer.kafka.user-actions-topic}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consume(UserActionAvro action) {
        log.info("Analyzer получил user-action: userId={}, eventId={}, actionType={}",
                action.getUserId(), action.getEventId(), action.getActionType());

        userActionUpdateService.updateInteraction(action);
    }
}
