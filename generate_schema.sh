#!/bin/bash

# Schema Generator Script
# This script reads .env variables and replaces bucket/scope names in schema files

set -e

# Default values
DEFAULT_BUCKET_NAME="ECOM"
DEFAULT_SCOPE_NAME="AI"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔧 Schema Generator Script${NC}"
echo "================================="

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠️  Warning: .env file not found. Using default values.${NC}"
    BUCKET_NAME=$DEFAULT_BUCKET_NAME
    SCOPE_NAME=$DEFAULT_SCOPE_NAME
else
    echo -e "${GREEN}✅ Found .env file${NC}"
    
    # Load environment variables from .env file
    export $(grep -v '^#' .env | xargs)
    
    # Get bucket and scope names from environment variables
    BUCKET_NAME=${COUCHBASE_BUCKET_NAME:-$DEFAULT_BUCKET_NAME}
    SCOPE_NAME=${COUCHBASE_SCOPE_NAME:-$DEFAULT_SCOPE_NAME}
fi

echo "📝 Configuration:"
echo "   Bucket Name: $BUCKET_NAME"
echo "   Scope Name: $SCOPE_NAME"
echo ""

# Create output directory
OUTPUT_DIR="schema_generated"
TEMPLATE_DIR="schema"

# Clean and create output directory
if [ -d "$OUTPUT_DIR" ]; then
    rm -rf "$OUTPUT_DIR"
fi
mkdir -p "$OUTPUT_DIR"

echo -e "${GREEN}📁 Processing schema files...${NC}"

# Process each schema file
for file in "$TEMPLATE_DIR"/*.sql "$TEMPLATE_DIR"/v*; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        echo "   Processing: $filename"
        
        # Replace bucket and scope names
        sed -e "s/\`ECOM\`/\`$BUCKET_NAME\`/g" \
            -e "s/\`AI\`/\`$SCOPE_NAME\`/g" \
            -e "s/\"ECOM\"/\"$BUCKET_NAME\"/g" \
            -e "s/\"AI\"/\"$SCOPE_NAME\"/g" \
            "$file" > "$OUTPUT_DIR/$filename"
    fi
done

# Copy non-SQL files (like .md)
for file in "$TEMPLATE_DIR"/*.md; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        echo "   Copying: $filename"
        cp "$file" "$OUTPUT_DIR/$filename"
    fi
done

echo ""
echo -e "${GREEN}✅ Schema generation completed!${NC}"
echo "📂 Generated files in: $OUTPUT_DIR/"
echo ""
echo -e "${YELLOW}📋 Next steps:${NC}"
echo "1. Review generated files in $OUTPUT_DIR/"
echo "2. Copy schema files to Couchbase container:"
echo "   docker cp $OUTPUT_DIR/ couchbase-ai:/tmp/schema/"
echo "3. Execute initialization scripts in order"
echo ""
echo -e "${GREEN}🎉 Ready to initialize your database!${NC}"