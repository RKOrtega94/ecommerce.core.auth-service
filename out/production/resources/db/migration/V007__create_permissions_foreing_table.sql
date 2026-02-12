SET
    search_path TO @@DATABASE_SCHEMA@@;

-- Create permissions_foreing table
CREATE
    FOREIGN TABLE IF NOT EXISTS remote_permissions
    (
        id UUID NOT NULL,
        name VARCHAR(100) NOT NULL,
        description VARCHAR(255),
        status status_enum NOT NULL,
        created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITHOUT TIME ZONE,
        deleted_at TIMESTAMP WITHOUT TIME ZONE,
        created_by VARCHAR(100),
        updated_by VARCHAR(100)
        ) SERVER security_db_server
    OPTIONS (schema_name '@@SECURITY_DB_SCHEMA@@', table_name 'permissions');

CREATE FOREIGN TABLE IF NOT EXISTS entity_permissions_foreing
    (
        entity_type VARCHAR(20) NOT NULL,
        entity_id UUID NOT NULL,
        permission_id UUID NOT NULL,
        status status_enum NOT NULL,
        created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITHOUT TIME ZONE,
        deleted_at TIMESTAMP WITHOUT TIME ZONE,
        created_by VARCHAR(100),
        updated_by VARCHAR(100)
        ) SERVER security_db_server
    OPTIONS (schema_name '@@SECURITY_DB_SCHEMA@@', table_name 'entity_permissions');