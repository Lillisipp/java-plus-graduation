package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.UserEventInteraction;
import ru.practicum.entity.UserEventInteractionId;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.repository.UserEventInteractionRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionUpdateService {

    private final UserEventInteractionRepository userEventInteractionRepository;

    @Transactional
    public void updateInteraction(UserActionAvro avro) {
        UserEventInteractionId id = new UserEventInteractionId(avro.getUserId(), avro.getEventId());

        double newWeight = mapWeight(avro.getActionType());

        UserEventInteraction entity = userEventInteractionRepository.findById(id)
                .orElseGet(() -> {
                    UserEventInteraction newEntity = new UserEventInteraction();
                    newEntity.setId(id);
                    newEntity.setWeight(0.0);
                    return newEntity;
                });

        if (newWeight > entity.getWeight()) {
            entity.setWeight(newWeight);
            entity.setUpdatedAt(avro.getTimestamp());
            userEventInteractionRepository.save(entity);

            log.info("Interaction обновлён: userId={}, eventId={}, weight={}",
                    avro.getUserId(), avro.getEventId(), newWeight);
        } else {
            log.debug("Interaction не обновлён (вес не вырос): userId={}, eventId={}",
                    avro.getUserId(), avro.getEventId());
        }
    }

    private double mapWeight(ru.practicum.ewm.stats.avro.ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}