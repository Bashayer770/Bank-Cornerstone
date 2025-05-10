#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "Running tests and checking coverage..."

# Run tests with coverage
./mvnw clean test jacoco:report

# Check if tests passed
if [ $? -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    
    # Check coverage
    COVERAGE=$(grep -o 'Total.*%' target/site/jacoco/index.html | grep -o '[0-9]*\.[0-9]*%')
    echo "Code coverage: $COVERAGE"
    
    # Verify minimum coverage threshold (80%)
    COVERAGE_NUM=$(echo $COVERAGE | sed 's/%//')
    if (( $(echo "$COVERAGE_NUM >= 80" | bc -l) )); then
        echo -e "${GREEN}Coverage meets minimum threshold (80%)${NC}"
    else
        echo -e "${RED}Coverage below minimum threshold (80%)${NC}"
        exit 1
    fi
else
    echo -e "${RED}Tests failed!${NC}"
    exit 1
fi

# Run integration tests separately
echo "Running integration tests..."
./mvnw test -Dtest=ShopIntegrationTest

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Integration tests passed!${NC}"
else
    echo -e "${RED}Integration tests failed!${NC}"
    exit 1
fi

echo "Test verification complete!" 