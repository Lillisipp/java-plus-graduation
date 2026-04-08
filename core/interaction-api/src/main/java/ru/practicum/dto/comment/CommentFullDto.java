package ru.practicum.dto.comment;

import lombok.*;
import ru.practicum.enums.comment.CommentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentFullDto {
    private Long id;
    private Long eventId;
    private Long authorId;
    private String text;
    private LocalDateTime createdOn;
    private CommentStatus status;
}