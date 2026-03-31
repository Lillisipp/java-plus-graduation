package ru.practicum.ewm.main.model.comment.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.main.model.comment.CommentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Builder
public class CommentFullDto {
    private Long id;
    private Long eventId;
    private Long authorId;
    private String text;
    private LocalDateTime createdOn;
    private CommentStatus status;
}