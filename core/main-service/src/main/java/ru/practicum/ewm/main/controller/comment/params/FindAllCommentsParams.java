package ru.practicum.ewm.main.controller.comment.params;

import lombok.*;
import ru.practicum.ewm.main.model.comment.CommentStatus;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindAllCommentsParams {
    private Long eventId;

    private Long userId;

    private CommentStatus status;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private Long from;

    private Long size;

    private String sort;
}
