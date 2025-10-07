#!/bin/bash

# Test script for Art Installations API endpoint

echo "Testing Art Installations API..."
echo "================================"

# Test event ID (UUID format)
EVENT_ID="550e8400-e29b-41d4-a716-446655440000"

# Base URL
BASE_URL="http://localhost:8080/api/v1"

echo ""
echo "1. Testing basic GET request (no location):"
echo "GET $BASE_URL/events/$EVENT_ID/art-installations"
echo ""
echo "Expected: List of non-hidden art installations only"
echo ""

echo ""
echo "2. Testing GET with location parameters:"
echo "GET $BASE_URL/events/$EVENT_ID/art-installations?lat=-30.7125&lng=23.9875"
echo ""
echo "Expected: List including hidden installations if within proximity"
echo ""

echo ""
echo "3. Testing with invalid event ID:"
echo "GET $BASE_URL/events/invalid-uuid/art-installations"
echo ""
echo "Expected: 400 Bad Request with error message"
echo ""

echo ""
echo "Note: Start the backend server with './gradlew run' before running these tests"
echo "You can test these endpoints using curl or a tool like Postman/Insomnia"