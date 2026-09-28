#!/bin/sh
set -e

BOOTSTRAP_SERVER="cinema-kafka-1:9092"

echo "Waiting for Kafka broker ($BOOTSTRAP_SERVER) to respond..."
until /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP_SERVER" --list > /dev/null 2>&1; do
  echo "Kafka is not available yet. Retrying in 2 seconds..."
  sleep 2
done

echo "Kafka is ready. Initializing topics..."

TOPICS="dev.cinema.sessions.v1 dev.cinema.payment-completed.v1 dev.cinema.payment-cancelled.v1 dev.cinema.refund-started.v1 dev.cinema.refund-completed.v1"

for TOPIC in $TOPICS; do
  echo "Ensuring topic '$TOPIC' exists with 3 partitions..."
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP_SERVER" \
    --create --if-not-exists \
    --topic "$TOPIC" \
    --partitions 3 \
    --replication-factor 1
done

echo "Kafka topics initialized successfully."
