package ru.practicum.service;

import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.util.Collection;
import java.util.List;

public interface RecommendationService {
    List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults);

    List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults);

    List<RecommendedEventProto> getInteractionsCount(Collection<Long> eventIds);
}