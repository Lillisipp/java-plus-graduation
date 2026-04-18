drop table if exists event_similarity, user_event_interaction;

create table if not exists event_similarity
(
    event_a    bigint           NOT NULL,
    event_b    bigint           NOT NULL,
    score      double precision NOT NULL,
    updated_at timestamp        NOT NULL,
    PRIMARY KEY (event_a, event_b)
);

create table if not exists user_event_interaction
(
    user_id    bigint           NOT NULL,
    event_id   bigint           NOT NULL,
    weight     double precision NOT NULL,
    updated_at timestamp        NOT NULL,
    PRIMARY KEY (user_id, event_id)
);