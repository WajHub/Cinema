ALTER TABLE payment_status_history
DROP CONSTRAINT IF EXISTS fk_payment_status_history_payment;

ALTER TABLE refund
DROP CONSTRAINT IF EXISTS fk_refund_payment;

ALTER TABLE payment
    ADD COLUMN id_uuid uuid DEFAULT gen_random_uuid() NOT NULL;

ALTER TABLE payment_status_history
    ADD COLUMN id_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    ADD COLUMN payment_id_uuid uuid;

ALTER TABLE refund
    ADD COLUMN id_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
    ADD COLUMN payment_id_uuid uuid;

ALTER TABLE stripe_webhook_event
    ADD COLUMN id_uuid uuid DEFAULT gen_random_uuid() NOT NULL;

UPDATE payment_status_history history
SET payment_id_uuid = payment.id_uuid
    FROM payment
WHERE history.payment_id = payment.id;

UPDATE refund
SET payment_id_uuid = payment.id_uuid
    FROM payment
WHERE refund.payment_id = payment.id;

ALTER TABLE payment_status_history
    ALTER COLUMN payment_id_uuid SET NOT NULL;

ALTER TABLE refund
    ALTER COLUMN payment_id_uuid SET NOT NULL;

ALTER TABLE payment_status_history
DROP COLUMN id,
    DROP COLUMN payment_id;
ALTER TABLE payment_status_history RENAME COLUMN id_uuid TO id;
ALTER TABLE payment_status_history RENAME COLUMN payment_id_uuid TO payment_id;

ALTER TABLE refund
DROP COLUMN id,
    DROP COLUMN payment_id;
ALTER TABLE refund RENAME COLUMN id_uuid TO id;
ALTER TABLE refund RENAME COLUMN payment_id_uuid TO payment_id;

ALTER TABLE stripe_webhook_event
DROP COLUMN id;
ALTER TABLE stripe_webhook_event RENAME COLUMN id_uuid TO id;

ALTER TABLE payment
DROP CONSTRAINT payment_pkey,
    DROP COLUMN id;
ALTER TABLE payment RENAME COLUMN id_uuid TO id;
ALTER TABLE payment ADD CONSTRAINT payment_pkey PRIMARY KEY (id);

ALTER TABLE payment_status_history
    ADD CONSTRAINT payment_status_history_pkey PRIMARY KEY (id),
    ADD CONSTRAINT fk_payment_status_history_payment
        FOREIGN KEY (payment_id) REFERENCES payment (id);

ALTER TABLE refund
    ADD CONSTRAINT refund_pkey PRIMARY KEY (id),
    ADD CONSTRAINT fk_refund_payment
        FOREIGN KEY (payment_id) REFERENCES payment (id);

ALTER TABLE payment
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE payment_status_history
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE refund
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE stripe_webhook_event
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE payment
    ADD CONSTRAINT chk_payment_status
        CHECK (current_status IN ('in_progress', 'completed', 'expired', 'cancelled', 'refund_pending', 'refunded'));

CREATE INDEX idx_payment_status_history_payment_id ON payment_status_history (payment_id);
CREATE INDEX idx_refund_payment_id ON refund (payment_id);
