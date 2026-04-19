package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.entity.UserEventInteraction;
import ru.practicum.entity.UserEventInteractionId;
import ru.practicum.projection.InteractionCountProjection;
import ru.practicum.projection.RecentInteractionProjection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserEventInteractionRepository extends JpaRepository<UserEventInteraction, UserEventInteractionId> {

    @Query("""
            select i.id.eventId
            from UserEventInteraction i
            where i.id.userId = :userId
            """)
    Set<Long> findEventIdsByUserId(long userId);

    @Query("""
            select i.id.eventId as eventId,
                   i.weight as weight,
                   i.updatedAt as updatedAt
            from UserEventInteraction i
            where i.id.userId = :userId
            order by i.updatedAt desc
            """)
    List<RecentInteractionProjection> findRecentByUserId(long userId, Pageable pageable);

    @Query("""
            select i.id.eventId as eventId,
                   sum(i.weight) as score
            from UserEventInteraction i
            where i.id.eventId in :eventIds
            group by i.id.eventId
            """)
    List<InteractionCountProjection> findInteractionCounts(Collection<Long> eventIds);
}

