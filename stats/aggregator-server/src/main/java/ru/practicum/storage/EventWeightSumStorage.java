package ru.practicum.storage;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventWeightSumStorage {

    private final Map<Long, Double> storage = new HashMap<>();

    public double getSum(long eventId) {
        return storage.getOrDefault(eventId, 0.0);
    }

    public void addDelta(long eventId, double delta) {
        storage.put(eventId, getSum(eventId) + delta);
    }
}