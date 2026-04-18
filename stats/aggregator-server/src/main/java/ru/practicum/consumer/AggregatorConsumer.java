package ru.practicum.consumer;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface AggregatorConsumer {
    void consume(UserActionAvro action);
}