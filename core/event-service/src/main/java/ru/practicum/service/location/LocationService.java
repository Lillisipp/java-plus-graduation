package ru.practicum.service.location;

import ru.practicum.model.Location;
import ru.practicum.dto.event.LocationDto;

public interface LocationService {
    Location saveLocation(LocationDto location);
}