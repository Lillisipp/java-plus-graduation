package ru.practicum.mapper.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.model.Comment;

@UtilityClass
public class CommentShortDtoMapper {
    public static CommentShortDto toDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .eventId(comment.getEventId())
                .authorId(comment.getAuthorId())
                .text(comment.getText())
                .createdOn(comment.getCreatedOn())
                .build();
    }
}