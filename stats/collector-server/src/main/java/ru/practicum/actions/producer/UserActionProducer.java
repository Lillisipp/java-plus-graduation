package ru.practicum.actions.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.actions.config.CollectorKafkaProperties;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserActionProducer {
    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;
    private final CollectorKafkaProperties collectorKafkaProperties;

    public void send(UserActionAvro message) {
        Long key = message.getUserId();

        log.debug("Отправка сообщения в Kafka: topic={}, key={}, eventId={}",
                collectorKafkaProperties.getUserActionsTopic(), key, message.getEventId());

        kafkaTemplate.send(collectorKafkaProperties.getUserActionsTopic(), key, message);

        log.info("Сообщение отправлено в Kafka: topic={}, userId={}, eventId={}, actionType={}",
                collectorKafkaProperties.getUserActionsTopic(),
                message.getUserId(),
                message.getEventId(),
                message.getActionType());
    }
}
