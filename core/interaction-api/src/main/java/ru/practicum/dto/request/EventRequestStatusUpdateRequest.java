package ru.practicum.dto.request;

import lombok.*;
import ru.practicum.enums.request.ParticipationRequestStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;

    ParticipationRequestStatus status;
}
