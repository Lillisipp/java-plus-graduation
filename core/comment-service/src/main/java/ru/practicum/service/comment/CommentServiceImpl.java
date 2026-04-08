package ru.practicum.service.comment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.user.UserClient;
import ru.practicum.controller.comment.params.FindAllCommentsParams;
import ru.practicum.dto.comment.CommentFullDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.comment.NewCommentRequest;
import ru.practicum.enums.comment.CommentStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.comment.CommentFullDtoMapper;
import ru.practicum.mapper.comment.CommentShortDtoMapper;
import ru.practicum.mapper.comment.NewCommentRequestMapper;
import ru.practicum.model.Comment;
import ru.practicum.repository.comment.CommentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;

    @Override
    public List<CommentShortDto> findAllCommentsForEvent(FindAllCommentsParams params) {
        return commentRepository.findAllComments(params).getContent()
                .stream()
                .map(CommentShortDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentFullDto findCommentById(Long eventId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!comment.getStatus().equals(CommentStatus.PUBLISHED)) {
            throw new NotFoundException("Комментарий еще не опубликован");
        }
        if (!comment.getEventId().equals(eventId)) {
            throw new ConflictException("Комментарий не относится к указанному событию");
        }
        return CommentFullDtoMapper.toDto(comment);
    }

    @Override
    public CommentFullDto addComment(Long eventId, Long authorId, NewCommentRequest request) {
        checkUserExists(authorId);
        Comment newComment = NewCommentRequestMapper.toEntity(request);
        newComment.setAuthorId(authorId);
        newComment.setEventId(eventId);
        newComment.setStatus(CommentStatus.PENDING);
        return CommentFullDtoMapper.toDto(commentRepository.save(newComment));
    }

    @Override
    public List<CommentFullDto> findCommentsByAuthorId(FindAllCommentsParams params) {
        return commentRepository.findAllComments(params).getContent()
                .stream()
                .map(CommentFullDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeCommentById(Long userId, Long commentId) {
        checkUserExists(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!userId.equals(comment.getAuthorId())) {
            throw new ConflictException("Пользователь не является автором комментария");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentFullDto> findAllComments(FindAllCommentsParams params) {
        return commentRepository.findAllComments(params).getContent()
                .stream()
                .map(CommentFullDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentFullDto moderateComment(Long commentId, CommentStatus status) {
        Comment updatingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        updatingComment.setStatus(status);
        return CommentFullDtoMapper.toDto(commentRepository.save(updatingComment));
    }

    @Override
    public void adminRemoveCommentById(Long commentId) {
        if (commentRepository.existsById(commentId)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Комментарий не найден");
        }
    }

    private void checkUserExists(Long userId) {
        try {
            var users = userClient.find(List.of(userId), 0, 1);
            if (users.isEmpty()) {
                throw new NotFoundException("Пользователь не найден");
            }
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Не удалось проверить пользователя id={}: {}", userId, e.getMessage());
        }
    }
}