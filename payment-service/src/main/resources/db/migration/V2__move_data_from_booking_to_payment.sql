ALTER TABLE payment
    ADD COLUMN booking_id bigint NOT NULL,
ADD COLUMN created_at timestamp NOT NULL DEFAULT current_timestamp;

UPDATE payment p
SET booking_id = b.booking_id,
    created_at = b.created_at
    FROM booking b
WHERE p.id = b.payment_id;

DROP TABLE booking;
