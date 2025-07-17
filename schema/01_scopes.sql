-- Create database scopes
-- This file should be executed first to create all required scopes

CREATE SCOPE `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}` IF NOT EXISTS;