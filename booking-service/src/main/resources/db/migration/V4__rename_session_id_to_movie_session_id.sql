DROP INDEX ux_session_seat_session_and_seat;

ALTER TABLE session_seat
RENAME COLUMN session_id TO movie_session_id;

ALTER TABLE session_seat
DROP CONSTRAINT fk_session_seat_session;

ALTER TABLE session_seat
ADD CONSTRAINT fk_session_seat_movie_session
    FOREIGN KEY (movie_session_id) REFERENCES movie_session (id);

CREATE UNIQUE INDEX ux_session_seat_movie_session_and_seat 
    ON session_seat (movie_session_id, seat_id);

DROP INDEX idx_session_seat_session_id;

CREATE INDEX idx_session_seat_movie_session_id 
    ON session_seat (movie_session_id);
