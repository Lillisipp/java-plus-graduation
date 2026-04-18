package ru.practicum.projection;

import java.time.Instant;

public interface RecentInteractionProjection {
    Long getEventId();
    Double getWeight();
    Instant getUpdatedAt();
}
