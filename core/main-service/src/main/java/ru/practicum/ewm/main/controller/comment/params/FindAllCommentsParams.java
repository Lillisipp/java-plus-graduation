package ru.practicum.ewm.main.controller.comment.params;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.main.model.comment.CommentStatus;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Builder
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
