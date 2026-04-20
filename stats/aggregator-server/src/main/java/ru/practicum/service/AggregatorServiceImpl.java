package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.ActionWeightMapper;
import ru.practicum.producer.EventSimilarityProducer;
import ru.practicum.storage.EventWeightSumStorage;
import ru.practicum.storage.MinWeightSumStorage;
import ru.practicum.storage.UserEventWeightStorage;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    private final UserEventWeightStorage userEventWeightStorage;
    private final EventWeightSumStorage eventWeightSumStorage;
    private final MinWeightSumStorage minWeightSumStorage;
    private final SimilarityCalculator similarityCalculator;
    private final EventSimilarityProducer eventSimilarityProducer;

    @Override
    public void process(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = ActionWeightMapper.toWeight(action.getActionType());
        double oldWeight = userEventWeightStorage.getWeight(userId, eventId);
        log.info("Обработка действия: userId={}, eventId={}, oldWeight={}, newWeight={}",
                userId, eventId, oldWeight, newWeight);
        if (newWeight <= oldWeight) {
            log.info("Максимальный вес не изменился, пересчёт не требуется: userId={}, eventId={}",
                    userId, eventId);
            return;
        }

        double weightDelta = newWeight - oldWeight;

        userEventWeightStorage.putWeight(userId, eventId, newWeight);
        eventWeightSumStorage.addDelta(eventId, weightDelta);

        Map<Long, Double> userWeights = userEventWeightStorage.getUserWeights(userId);

        for (Map.Entry<Long, Double> entry : userWeights.entrySet()) {
            long otherEventId = entry.getKey();
            double otherWeight = entry.getValue();

            if (otherEventId == eventId) {
                continue;
            }

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double minDelta = newMin - oldMin;

            if (minDelta != 0.0) {
                minWeightSumStorage.addDelta(eventId, otherEventId, minDelta);
            }

            double minSum = minWeightSumStorage.getSum(eventId, otherEventId);
            double sumA = eventWeightSumStorage.getSum(eventId);
            double sumB = eventWeightSumStorage.getSum(otherEventId);

            double score = similarityCalculator.calculate(minSum, sumA, sumB);

            EventSimilarityAvro similarity = new EventSimilarityAvro();
            similarity.setEventA(Math.min(eventId, otherEventId));
            similarity.setEventB(Math.max(eventId, otherEventId));
            similarity.setScore(score);
            similarity.setTimestamp(action.getTimestamp());

            eventSimilarityProducer.send(similarity);

            log.info("Similarity пересчитан: eventA={}, eventB={}, minSum={}, sumA={}, sumB={}, score={}",
                    similarity.getEventA(), similarity.getEventB(), minSum, sumA, sumB, score);
        }
    }
}