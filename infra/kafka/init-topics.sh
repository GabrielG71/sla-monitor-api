#!/bin/sh

set -e

BOOTSTRAP="kafka:9092"
RF=1

echo "Waiting for Kafka to be ready..."

until kafka-topics --bootstrap-server "$BOOTSTRAP" --list > /dev/null 2>&1; do
    sleep 2
done

echo "Kafka is ready."

create_topic() {
    topic=$1
    partitions=$2

    if kafka-topics --bootstrap-server "$BOOTSTRAP" --list | grep -q "^${topic}$"; then
        echo "Topic already exists: $topic"
    else
        kafka-topics --bootstrap-server "$BOOTSTRAP" \
            --create \
            --topic "$topic" \
            --partitions "$partitions" \
            --replication-factor "$RF"
        echo "Created topic: $topic"
    fi
}

create_topic "raw-checks"       6
create_topic "sla-ok"           3
create_topic "sla-violations"   3
create_topic "raw-checks.DLT"   1

echo "All topics created."