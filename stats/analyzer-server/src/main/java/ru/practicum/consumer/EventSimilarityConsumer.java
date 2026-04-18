package ru.practicum.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.EventSimilarityUpdateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final EventSimilarityUpdateService eventSimilarityUpdateService;

    @KafkaListener(
            topics = "${analyzer.kafka.events-similarity-topic}",
            containerFactory = "eventSimilarityKafkaListenerContainerFactory"
    )
    public void consume(EventSimilarityAvro similarity) {
        log.info("Analyzer получил similarity: eventA={}, eventB={}, score={}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());

        eventSimilarityUpdateService.updateSimilarity(similarity);
    }
}
