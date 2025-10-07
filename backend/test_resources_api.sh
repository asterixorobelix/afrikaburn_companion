#!/bin/bash

echo "Testing Resource Locations API"
echo "=============================="

# Test valid UUID
EVENT_ID="f47ac10b-58cc-4372-a567-0e02b2c3d479"
PORT=8080

echo "1. Testing GET /api/v1/events/$EVENT_ID/resource-locations"
echo "Expected: List of all resource locations"
echo ""

echo "2. Testing with resource_type=water filter"
echo "GET /api/v1/events/$EVENT_ID/resource-locations?resource_type=water"
echo ""

echo "3. Testing with resource_type=ice filter"
echo "GET /api/v1/events/$EVENT_ID/resource-locations?resource_type=ice"
echo ""

echo "4. Testing with resource_type=help filter"
echo "GET /api/v1/events/$EVENT_ID/resource-locations?resource_type=help"
echo ""

echo "5. Testing with invalid resource_type"
echo "GET /api/v1/events/$EVENT_ID/resource-locations?resource_type=invalid"
echo "Expected: 400 Bad Request with error message"
echo ""

echo "6. Testing with invalid event ID"
echo "GET /api/v1/events/invalid-uuid/resource-locations"
echo "Expected: 400 Bad Request with error message"
echo ""

echo "To run these tests when the server is running:"
echo "curl http://localhost:$PORT/api/v1/events/$EVENT_ID/resource-locations"
echo "curl http://localhost:$PORT/api/v1/events/$EVENT_ID/resource-locations?resource_type=water"
echo "curl http://localhost:$PORT/api/v1/events/$EVENT_ID/resource-locations?resource_type=ice"