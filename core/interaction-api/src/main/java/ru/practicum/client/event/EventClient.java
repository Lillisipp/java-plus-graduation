package ru.practicum.client.event;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.

        EventFullDto;

@FeignClient(name = "event-server")
public interface EventClient {

    @GetMapping("/admin/events/{eventId}/existence/{initiatorId}")
    boolean findExistEventByEventIdAndInitiatorId(@PathVariable @Positive Long eventId,
                                                  @PathVariable @Positive Long initiatorId);

    @GetMapping("/admin/events/{eventId}/existence")
    boolean findExistEventByEventId(@PathVariable @Positive Long eventId);

    @GetMapping("/admin/events/{eventId}")
    EventFullDto findEventById(@PathVariable Long eventId);
}