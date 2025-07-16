# Database Setup Guide

Execute the schema files in the following order:

## 1. Initial Setup
```bash
# Run in Couchbase Query Workbench or via cbq
cbq -e "couchbase://localhost" -u admin -p couchbase < v0.0_init
```

## 2. User & RBAC Setup
```bash
# Create user and role collections with indexes
cbq -e "couchbase://localhost" -u admin -p couchbase < v0.1_user_rbac

# Insert initial test data (users, teams, datasets)
cbq -e "couchbase://localhost" -u admin -p couchbase < v0.1_user_rbac_test_data
```

## 3. Team Roles Setup
```bash
# Create team role collection, indexes, and insert default roles
cbq -e "couchbase://localhost" -u admin -p couchbase < v0.2_team_role
```

## 4. System Roles Initialization
```bash
# Insert system roles
cbq -e "couchbase://localhost" -u admin -p couchbase < v0.3_system_role_init
```

## Collections Created

### Core Application
- `document` - File documents and metadata
- `dataset` - Data collections with access control
- `chat-topic` - Chat conversation topics
- `chat-record` - Individual chat interactions
- `chat-message` - Chat messages storage
- `document-vector` - Vector embeddings for AI

### Authentication & Authorization
- `user` - User accounts
- `team` - Teams/organizations
- `team-membership` - User-team relationships
- `team-role` - Team-specific roles
- `system-role` - System-wide roles

### System
- `cache` - AI and application caching

## Default Users Created

| Username | Email | Password | System Role |
|----------|-------|----------|-------------|
| super_admin | super_admin@example.com | password123 | SUPER_ADMIN |
| user_admin | user_admin@example.com | password123 | USER_ADMIN |
| team_admin | team_admin@example.com | password123 | TEAM_ADMIN |

## Verification

After setup, verify collections exist:
```sql
SELECT name FROM system:keyspaces 
WHERE bucket_name = 'ECOM' AND scope_name = 'AI';
```

Verify users exist:
```sql
SELECT name, email, systemRoles FROM `ECOM`.`AI`.`user`;
```