#!/bin/bash
set -e

echo "Starting server..."
gradle run &
SERVER_PID=$!

# Wait for server to start
echo "Waiting for server to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080/health > /dev/null; then
        echo "Server is ready!"
        break
    fi
    sleep 1
done

echo "Testing weather alerts endpoint..."
curl -s -X GET "http://localhost:8080/events/123e4567-e89b-12d3-a456-426614174000/weather-alerts" | jq

# Kill the server
echo "Stopping server..."
kill $SERVER_PID 2>/dev/null || true