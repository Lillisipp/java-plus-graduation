package ru.practicum.repository.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>, QCommentRepository {

    void deleteCommentsByEventId(Long eventId);
}