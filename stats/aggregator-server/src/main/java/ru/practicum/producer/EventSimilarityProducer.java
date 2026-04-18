package ru.practicum.producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.config.AggregatorKafkaProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProducer {

    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;
    private final AggregatorKafkaProperties properties;

    public void send(EventSimilarityAvro message) {
        Long key = message.getEventA();

        log.debug("Отправка similarity в Kafka: eventA={}, eventB={}, score={}",
                message.getEventA(), message.getEventB(), message.getScore());

        kafkaTemplate.send(properties.getEventsSimilarityTopic(), key, message);

        log.info("Similarity отправлен в Kafka: eventA={}, eventB={}, score={}",
                message.getEventA(), message.getEventB(), message.getScore());
    }
}