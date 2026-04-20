package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.EventSimilarity;
import ru.practicum.entity.EventSimilarityId;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.repository.EventSimilarityRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityUpdateService {

    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    public void updateSimilarity(EventSimilarityAvro avro) {
        EventSimilarityId id = new EventSimilarityId(avro.getEventA(), avro.getEventB());

        EventSimilarity entity = eventSimilarityRepository.findById(id)
                .orElseGet(() -> {
                    EventSimilarity newEntity = new EventSimilarity();
                    newEntity.setId(id);
                    return newEntity;
                });

        entity.setScore(avro.getScore());
        entity.setUpdatedAt(avro.getTimestamp());

        eventSimilarityRepository.save(entity);

        log.info("Similarity сохранён: eventA={}, eventB={}, score={}",
                avro.getEventA(), avro.getEventB(), avro.getScore());
    }
}