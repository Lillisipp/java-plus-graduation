package ru.practicum.mapper.request;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

@UtilityClass
public class ParticipationRequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest model) {
        return ParticipationRequestDto.builder()
                .id(model.getId())
                .created(model.getCreated())
                .event(model.getEventId())
                .requester(model.getRequesterId())
                .status(model.getStatus().toString())
                .build();
    }
}