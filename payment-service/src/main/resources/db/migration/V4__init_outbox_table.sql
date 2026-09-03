create table outbox_event
(
    id             uuid primary key,
    aggregate_type varchar(255) not null,
    aggregate_id   uuid         not null,
    type           varchar(255) not null,
    payload        jsonb        not null,
    created_at     timestamptz  not null default current_timestamp,
    processed_at   timestamptz
);

create index idx_outbox_event_processed_at on outbox_event (processed_at);
create index idx_outbox_event_created_at on outbox_event (created_at);
