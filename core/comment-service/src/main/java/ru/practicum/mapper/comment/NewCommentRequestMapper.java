package ru.practicum.mapper.comment;

import ru.practicum.dto.comment.NewCommentRequest;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;

public class NewCommentRequestMapper {
    public static Comment toEntity(NewCommentRequest comment) {
        return Comment.builder()
                .text(comment.getText())
                .createdOn(LocalDateTime.now())
                .build();
    }
}