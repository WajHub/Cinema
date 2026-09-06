ALTER TABLE refund
    ALTER COLUMN amount TYPE numeric(19, 2) USING amount / 100.0;

ALTER TABLE refund RENAME COLUMN amount TO total_price;
