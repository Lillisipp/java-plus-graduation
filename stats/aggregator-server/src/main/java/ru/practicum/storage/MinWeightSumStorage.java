package ru.practicum.storage;


import org.springframework.stereotype.Component;
import ru.practicum.EventPair;

import java.util.HashMap;
import java.util.Map;

@Component
public class MinWeightSumStorage {

    private final Map<Long, Map<Long, Double>> storage = new HashMap<>();

    public double getSum(long eventA, long eventB) {
        EventPair pair = EventPair.of(eventA, eventB);

        return storage.getOrDefault(pair.first(), Map.of())
                .getOrDefault(pair.second(), 0.0);
    }

    public void addDelta(long eventA, long eventB, double delta) {
        EventPair pair = EventPair.of(eventA, eventB);

        storage.computeIfAbsent(pair.first(), id -> new HashMap<>())
                .merge(pair.second(), delta, Double::sum);
    }
}