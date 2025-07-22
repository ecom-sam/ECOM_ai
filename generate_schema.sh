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
    USERNAME="admin"
    PASSWORD="couchbase"
else
    echo -e "${GREEN}✅ Found .env file${NC}"
    
    # Load environment variables from .env file
    export $(grep -v '^#' .env | xargs)
    
    # Get bucket and scope names from environment variables
    BUCKET_NAME=${COUCHBASE_BUCKET_NAME:-$DEFAULT_BUCKET_NAME}
    SCOPE_NAME=${COUCHBASE_SCOPE_NAME:-$DEFAULT_SCOPE_NAME}
    USERNAME=${COUCHBASE_USERNAME:-admin}
    PASSWORD=${COUCHBASE_PASSWORD:-couchbase}
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
        
        # Replace environment variable placeholders with actual values
        sed -e "s/\${COUCHBASE_BUCKET_NAME}/$BUCKET_NAME/g" \
            -e "s/\${COUCHBASE_SCOPE_NAME}/$SCOPE_NAME/g" \
            "$file" > "$OUTPUT_DIR/$filename"
    fi
done

# Generate initialization script
echo "   Generating: init_couchbase.sh"
cat > "$OUTPUT_DIR/init_couchbase.sh" << 'EOF'
#!/bin/bash

# Couchbase Database Initialization Script
# This script initializes Couchbase with your environment configuration

set -e

echo "🚀 Starting Couchbase Database Initialization"
echo "============================================="

# Check if Couchbase container is running
if ! docker ps | grep -q couchbase-ai; then
    echo "❌ Error: Couchbase container 'couchbase-ai' is not running"
    echo "Please start the container first: docker run ... couchbase-ai"
    exit 1
fi

echo "✅ Couchbase container is running"

# Step 1: Create Bucket via REST API
echo ""
echo "📦 Step 1: Creating Bucket..."
EOF

cat >> "$OUTPUT_DIR/init_couchbase.sh" << EOF
curl -u $USERNAME:$PASSWORD -X POST http://localhost:8091/pools/default/buckets \\
  -d name=$BUCKET_NAME \\
  -d bucketType=couchbase \\
  -d ramQuotaMB=512 \\
  -d authType=sasl

echo "⏳ Waiting for bucket to be ready..."
sleep 5

# Step 2: Execute all schema initialization in one command
echo ""
echo "📋 Step 2: Executing schema initialization..."

docker exec couchbase-ai bash -c '
echo "📂 Creating scope..."
cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -s "CREATE SCOPE \\\`$BUCKET_NAME\\\`.\\\`$SCOPE_NAME\\\` IF NOT EXISTS;"

echo "📂 Creating scopes from file..."
cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/01_scopes.sql

echo "⏳ Waiting for scopes to be ready..."
sleep 3

echo "📋 Creating collections..."
cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/02_collections.sql

echo "⏳ Waiting for collections to be ready..."
sleep 5

echo "📊 Inserting initial data..."
cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/03_data_users.sql
cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/03_data_system_roles.sql
cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/03_data_team_roles.sql

echo "⏳ Waiting for data insertion to complete..."
sleep 3

echo "🔍 Creating indexes..."
cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/04_indexes.sql
'

echo ""
echo "🎉 Database initialization completed successfully!"
echo ""
echo "📊 Configuration Summary:"
echo "   Bucket: $BUCKET_NAME"
echo "   Scope: $SCOPE_NAME"
echo "   Username: $USERNAME"
echo ""
echo "🔍 You can verify the setup by accessing:"
echo "   Couchbase Web Console: http://localhost:8091"
echo "   Login with: $USERNAME / $PASSWORD"
echo ""
echo "📂 Schema files executed in order:"
echo "   1. 01_scopes.sql - Database scopes"
echo "   2. 02_collections.sql - All collections"
echo "   3. 03_data_*.sql - Initial data (users, system roles, team roles)"
echo "   4. 04_indexes.sql - Database indexes"
EOF

chmod +x "$OUTPUT_DIR/init_couchbase.sh"

echo ""
echo -e "${GREEN}✅ Schema generation completed!${NC}"
echo "📂 Generated files in: $OUTPUT_DIR/"
echo ""
echo -e "${YELLOW}📋 Next steps:${NC}"
echo "1. Review generated files in $OUTPUT_DIR/"
echo "2. Copy schema files to Couchbase container:"
echo "   docker cp $OUTPUT_DIR/. couchbase-ai:/tmp/schema/"
echo "3. Execute the initialization script:"
echo "   bash $OUTPUT_DIR/init_couchbase.sh"
echo ""
echo -e "${GREEN}🎉 Ready to initialize your database!${NC}"