create table cinema (
    id uuid primary key,
    name varchar(255) not null,
    address varchar(255) not null,
    city varchar(255) not null,
    is_active boolean not null,
    created_at timestamptz not null default current_timestamp
);

create table auditory (
    id uuid primary key,
    cinema_id uuid not null,
    name varchar(255) not null,
    capacity integer not null,
    is_active boolean not null,
    constraint fk_auditory_cinema
        foreign key (cinema_id) references cinema (id)
);

create index idx_auditory_cinema_id on auditory (cinema_id);

create table seat (
    id uuid primary key,
    auditory_id uuid not null,
    row_label varchar(255) not null,
    seat_number integer not null,
    seat_price numeric(12, 2) not null,
    seat_type varchar(255) not null,
    constraint fk_seat_auditory
        foreign key (auditory_id) references auditory (id)
);

create index idx_seat_auditory_id on seat (auditory_id);

create table movie (
    id uuid primary key,
    title varchar(255) not null,
    description varchar(4000) not null,
    poster_url varchar(255) not null,
    genres text[] not null,
    duration_minutes integer not null,
    language varchar(255) not null,
    age_rating varchar(255) not null,
    release_date date not null,
    is_active boolean not null
);

create table session (
    id uuid primary key,
    auditory_id uuid not null,
    movie_id uuid not null,
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    base_price numeric(12, 2) not null,
    status varchar(255) not null,
    constraint fk_session_auditory
        foreign key (auditory_id) references auditory (id),
    constraint fk_session_movie
        foreign key (movie_id) references movie (id)
);

create index idx_session_auditory_id on session (auditory_id);
create index idx_session_movie_id on session (movie_id);

create table session_movie_history (
    id uuid primary key,
    original_session_id varchar(255) not null,
    cinema_name varchar(255) not null,
    cinema_city varchar(255) not null,
    auditory_name varchar(255) not null,
    movie_title varchar(255) not null,
    movie_description varchar(4000) not null,
    movie_poster_url varchar(255) not null,
    movie_genres text[] not null,
    movie_duration_minutes integer not null,
    movie_language varchar(255) not null,
    movie_age_rating varchar(255) not null,
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    base_price numeric(12, 2) not null,
    session_status varchar(255) not null,
    recorded_at timestamptz not null default current_timestamp,
    archived_reason varchar(1000)
);

create index idx_session_movie_history_recorded_at on session_movie_history (recorded_at);
