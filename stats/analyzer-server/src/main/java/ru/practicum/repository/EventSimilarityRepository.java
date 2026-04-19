package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.entity.EventSimilarity;
import ru.practicum.entity.EventSimilarityId;
import ru.practicum.projection.NeighborProjection;
import ru.practicum.projection.SimilarEventProjection;

import java.util.Collection;
import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {
    @Query("""
            select case
                     when s.id.eventA = :eventId then s.id.eventB
                     else s.id.eventA
                   end as eventId,
                   s.score as score
            from EventSimilarity s
            where s.id.eventA = :eventId or s.id.eventB = :eventId
            order by s.score desc
            """)
    List<SimilarEventProjection> findSimilarEvents(long eventId, Pageable pageable);

    @Query("""
            select case
                     when s.id.eventA = :candidateEventId then s.id.eventB
                     else s.id.eventA
                   end as eventId,
                   s.score as score
            from EventSimilarity s
            where (s.id.eventA = :candidateEventId and s.id.eventB in :neighborIds)
               or (s.id.eventB = :candidateEventId and s.id.eventA in :neighborIds)
            order by s.score desc
            """)
    List<NeighborProjection> findNeighbors(long candidateEventId, Collection<Long> neighborIds, Pageable pageable);
}
