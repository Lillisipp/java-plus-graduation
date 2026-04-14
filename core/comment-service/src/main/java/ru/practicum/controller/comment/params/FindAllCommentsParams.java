package ru.practicum.controller.comment.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.enums.comment.CommentStatus;

import java.time.LocalDateTime;

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