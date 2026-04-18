package ru.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_event_interaction")
public class UserEventInteraction {
    @EmbeddedId
    private UserEventInteractionId id;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
