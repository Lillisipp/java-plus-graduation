package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.enums.comment.CommentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Column(name = "user_id", nullable = false)
    Long authorId;

    @Column(name = "comment_text")
    String text;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    CommentStatus status;
}