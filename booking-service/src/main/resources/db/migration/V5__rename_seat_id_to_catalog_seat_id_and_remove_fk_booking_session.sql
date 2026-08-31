DROP INDEX idx_booking_session_id;

ALTER TABLE booking 
    DROP COLUMN session_id;

ALTER TABLE session_seat 
    RENAME COLUMN seat_id TO catalog_seat_id;

ALTER INDEX ux_session_seat_movie_session_and_seat 
    RENAME TO ux_session_seat_movie_session_and_catalog_seat;