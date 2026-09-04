ALTER TABLE payment
    DROP CONSTRAINT IF EXISTS chk_payment_status;

ALTER TABLE payment_status_history
    DROP CONSTRAINT IF EXISTS chk_payment_status_history_status;

ALTER TABLE refund
    DROP CONSTRAINT IF EXISTS chk_refund_status;

UPDATE payment
SET current_status = upper(current_status);

UPDATE payment_status_history
SET status = upper(status);

UPDATE refund
SET status = upper(status);

ALTER TABLE payment
    ADD CONSTRAINT chk_payment_status
        CHECK (current_status IN ('IN_PROGRESS', 'COMPLETED', 'EXPIRED', 'CANCELLED', 'REFUND_PENDING', 'REFUNDED'));

ALTER TABLE payment_status_history
    ADD CONSTRAINT chk_payment_status_history_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'EXPIRED', 'CANCELLED', 'REFUND_PENDING', 'REFUNDED'));

ALTER TABLE refund
    ADD CONSTRAINT chk_refund_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED'));
