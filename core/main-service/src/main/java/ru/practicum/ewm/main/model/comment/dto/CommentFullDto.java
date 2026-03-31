package ru.practicum.ewm.main.model.comment.dto;

import lombok.*;
import ru.practicum.ewm.main.model.comment.CommentStatus;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
public class CommentFullDto {
    private Long id;
    private Long eventId;
    private Long authorId;
    private String text;
    private LocalDateTime createdOn;
    private CommentStatus status;
}