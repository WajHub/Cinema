ALTER TABLE payment
    ALTER COLUMN booking_id TYPE uuid USING booking_id::text::uuid,
    ALTER COLUMN amount TYPE numeric(19, 2) USING amount / 100.0;

ALTER TABLE payment RENAME COLUMN amount TO total_price;

ALTER TABLE payment
    ADD COLUMN catalog_session_id uuid NOT NULL,
    ADD COLUMN catalog_seat_ids jsonb NOT NULL,
    ADD COLUMN started_at timestamptz NOT NULL;

ALTER TABLE payment
    ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

ALTER TABLE payment
    ADD CONSTRAINT uk_payment_booking_id UNIQUE (booking_id);

CREATE INDEX idx_payment_expiration
    ON payment (current_status, started_at);