package ru.practicum.repository.events;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.model.Events;
import ru.practicum.model.params.PublicEventSearchParams;

public interface EventPublicQueryRepository {

    Page<Events> findPublicEvents(PublicEventSearchParams params, Pageable pageable);
}