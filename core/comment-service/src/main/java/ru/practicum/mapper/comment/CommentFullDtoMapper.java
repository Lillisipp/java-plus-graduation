package ru.practicum.mapper.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.CommentFullDto;
import ru.practicum.model.Comment;

@UtilityClass
public class CommentFullDtoMapper {

    public static CommentFullDto toDto(Comment comment) {
        return CommentFullDto.builder()
                .id(comment.getId())
                .eventId(comment.getEventId())
                .authorId(comment.getAuthorId())
                .text(comment.getText())
                .createdOn(comment.getCreatedOn())
                .status(comment.getStatus())
                .build();
    }
}