package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.projection.InteractionCountProjection;
import ru.practicum.projection.NeighborProjection;
import ru.practicum.projection.RecentInteractionProjection;
import ru.practicum.projection.SimilarEventProjection;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserEventInteractionRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final UserEventInteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;
    private final PredictionCalculator predictionCalculator;

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        log.info("Получение рекомендаций для userId={}, maxResults={}", userId, maxResults);

        Set<Long> interactedEventIds = interactionRepository.findEventIdsByUserId(userId);
        if (interactedEventIds.isEmpty()) {
            log.info("Пользователь userId={} не имеет взаимодействий", userId);
            return List.of();
        }

        Pageable recentLimit = PageRequest.of(0, maxResults);
        List<RecentInteractionProjection> recentInteractions =
                interactionRepository.findRecentByUserId(userId, recentLimit);

        List<Long> recentEventIds = recentInteractions.stream()
                .map(RecentInteractionProjection::getEventId)
                .toList();

        // Кандидаты — события, похожие на недавние, но с которыми пользователь ещё не взаимодействовал
        Pageable candidateLimit = PageRequest.of(0, maxResults * 5);
        Set<Long> candidateIds = recentEventIds.stream()
                .flatMap(eventId -> similarityRepository.findSimilarEvents(eventId, candidateLimit).stream())
                .map(SimilarEventProjection::getEventId)
                .filter(eventId -> !interactedEventIds.contains(eventId))
                .collect(Collectors.toSet());

        if (candidateIds.isEmpty()) {
            log.info("Нет кандидатов для рекомендаций userId={}", userId);
            return List.of();
        }

        // Для каждого кандидата считаем предсказанный score
        List<RecommendedEventProto> predictions = new ArrayList<>();
        Pageable neighborLimit = PageRequest.of(0, maxResults);

        for (Long candidateId : candidateIds) {
            List<NeighborProjection> neighbors =
                    similarityRepository.findNeighbors(candidateId, interactedEventIds, neighborLimit);

            if (neighbors.isEmpty()) {
                continue;
            }

            List<Double> similarities = neighbors.stream()
                    .map(NeighborProjection::getScore)
                    .toList();

            List<Double> weights = neighbors.stream()
                    .map(n -> recentInteractions.stream()
                            .filter(r -> r.getEventId().equals(n.getEventId()))
                            .map(RecentInteractionProjection::getWeight)
                            .findFirst()
                            .orElse(0.0))
                    .toList();

            double score = predictionCalculator.calculate(similarities, weights);

            if (score > 0.0) {
                predictions.add(RecommendedEventProto.newBuilder()
                        .setEventId(candidateId)
                        .setScore(score)
                        .build());
            }
        }

        return predictions.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(maxResults)
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.info("Получение похожих событий: eventId={}, userId={}, maxResults={}", eventId, userId, maxResults);

        Set<Long> interactedEventIds = interactionRepository.findEventIdsByUserId(userId);

        Pageable pageable = PageRequest.of(0, maxResults + interactedEventIds.size());
        List<SimilarEventProjection> similar = similarityRepository.findSimilarEvents(eventId, pageable);

        return similar.stream()
                .filter(s -> !interactedEventIds.contains(s.getEventId()))
                .limit(maxResults)
                .map(s -> RecommendedEventProto.newBuilder()
                        .setEventId(s.getEventId())
                        .setScore(s.getScore())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(Collection<Long> eventIds) {
        log.info("Получение суммы взаимодействий: eventIds={}", eventIds);

        List<InteractionCountProjection> counts = interactionRepository.findInteractionCounts(eventIds);

        return counts.stream()
                .map(c -> RecommendedEventProto.newBuilder()
                        .setEventId(c.getEventId())
                        .setScore(c.getScore())
                        .build())
                .toList();
    }
}