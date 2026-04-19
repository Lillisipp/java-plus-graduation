package ru.practicum.storage;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserEventWeightStorage {

    private final Map<Long, Map<Long, Double>> storage = new HashMap<>();

    public double getWeight(long userId, long eventId) {
        return storage.getOrDefault(userId, Collections.emptyMap())
                .getOrDefault(eventId, 0.0);
    }

    public void putWeight(long userId, long eventId, double weight) {
        storage.computeIfAbsent(userId, id -> new HashMap<>())
                .put(eventId, weight);
    }

    public Map<Long, Double> getUserWeights(long userId) {
        return storage.getOrDefault(userId, Collections.emptyMap());
    }
}