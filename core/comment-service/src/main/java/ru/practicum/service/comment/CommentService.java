package ru.practicum.service.comment;

import ru.practicum.controller.comment.params.FindAllCommentsParams;
import ru.practicum.dto.comment.CommentFullDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.comment.NewCommentRequest;
import ru.practicum.enums.comment.CommentStatus;

import java.util.List;

public interface CommentService {

    List<CommentShortDto> findAllCommentsForEvent(FindAllCommentsParams params);

    CommentFullDto findCommentById(Long eventId, Long commentId);

    CommentFullDto addComment(Long eventId, Long authorId, NewCommentRequest comment);

    List<CommentFullDto> findCommentsByAuthorId(FindAllCommentsParams params);

    void removeCommentById(Long userId, Long commentId);

    List<CommentFullDto> findAllComments(FindAllCommentsParams params);

    CommentFullDto moderateComment(Long commentId, CommentStatus status);

    void adminRemoveCommentById(Long commentId);
}