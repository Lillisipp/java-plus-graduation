package ru.practicum.repository.events;

import org.springframework.data.domain.Pageable;
import ru.practicum.model.Events;
import ru.practicum.model.params.AdminEventSearchParams;

import java.util.List;

public interface EventAdminQueryRepository {
    List<Events> findAdminEvents(AdminEventSearchParams params, Pageable pageable);
}