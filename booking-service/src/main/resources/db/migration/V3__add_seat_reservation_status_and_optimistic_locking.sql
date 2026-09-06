ALTER TABLE booking
ADD COLUMN version bigint DEFAULT 0 NOT NULL;

ALTER TABLE booking
DROP CONSTRAINT chk_booking_status;

ALTER TABLE booking
ADD CONSTRAINT chk_booking_status
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'));

ALTER TABLE session_seat
ADD COLUMN status varchar(32) DEFAULT 'AVAILABLE' NOT NULL,
ADD COLUMN version bigint DEFAULT 0 NOT NULL;

ALTER TABLE session_seat
ADD CONSTRAINT chk_session_seat_status
    CHECK (status IN ('AVAILABLE', 'TEMPORARY', 'CONFIRMED', 'CANCELLED', 'USED'));
