create table movie_session (
    id uuid primary key,
    catalog_session_id uuid not null unique,
    movie_title varchar(255) not null,
    starts_at timestamptz not null,
    ends_at timestamptz not null
);

create table "user" (
    id uuid primary key,
    email varchar(255) not null unique,
    name varchar(255) not null
);

create table booking (
    id uuid primary key,
    user_id uuid not null,
    session_id uuid not null,
    total_price numeric(12, 2) not null,
    status varchar(32) not null,
    constraint fk_booking_user
        foreign key (user_id) references "user" (id),
    constraint fk_booking_session
        foreign key (session_id) references movie_session (id),
    constraint chk_booking_status
        check (status in ('TEMPORARY', 'CONFIRMED', 'USED', 'CANCELLED'))
);

create index idx_booking_user_id on booking (user_id);
create index idx_booking_session_id on booking (session_id);

create table session_seat (
    id uuid primary key,
    session_id uuid not null,
    booking_id uuid,
    seat_id uuid not null,
    row_label varchar(255) not null,
    seat_number integer not null,
    final_price numeric(12, 2) not null,
    constraint fk_session_seat_session
        foreign key (session_id) references movie_session (id),
    constraint fk_session_seat_booking
        foreign key (booking_id) references booking (id)
);

create index idx_session_seat_session_id on session_seat (session_id);
create index idx_session_seat_booking_id on session_seat (booking_id);
create unique index ux_session_seat_session_and_seat on session_seat (session_id, seat_id);

create table booking_history (
    id uuid primary key,
    original_booking_id uuid not null,
    user_id uuid,
    movie_title varchar(255) not null,
    total_price numeric(12, 2) not null,
    archived_at timestamptz not null default current_timestamp,
    details jsonb,
    session_movie_history uuid
);

create index idx_booking_history_original_booking_id on booking_history (original_booking_id);
create index idx_booking_history_user_id on booking_history (user_id);
create index idx_booking_history_archived_at on booking_history (archived_at);