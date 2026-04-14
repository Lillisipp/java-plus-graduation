package ru.practicum.repository.comment;

import org.springframework.data.domain.Page;
import ru.practicum.controller.comment.params.FindAllCommentsParams;
import ru.practicum.model.Comment;

public interface QCommentRepository {

    Page<Comment> findAllComments(FindAllCommentsParams params);
}