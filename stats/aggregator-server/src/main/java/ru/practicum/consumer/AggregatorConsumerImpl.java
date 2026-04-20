package ru.practicum.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.AggregatorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorConsumerImpl implements AggregatorConsumer {
    private final AggregatorService aggregatorService;

    @KafkaListener(
            topics = "${aggregator.kafka.user-actions-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Override
    public void consume(UserActionAvro action) {
        log.info("Получено сообщение из Kafka: userId={}, eventId={}, actionType={}",
                action.getUserId(), action.getEventId(), action.getActionType());

        aggregatorService.process(action);
    }
}
