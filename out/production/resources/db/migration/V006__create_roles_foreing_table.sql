SET
    search_path TO @@DATABASE_SCHEMA@@;

-- Create roles_foreing table
CREATE
    FOREIGN TABLE IF NOT EXISTS remote_roles
    (
        id UUID NOT NULL,
        name VARCHAR(50) NOT NULL,
        status VARCHAR(20) NOT NULL,
        created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITHOUT TIME ZONE,
        deleted_at TIMESTAMP WITHOUT TIME ZONE,
        created_by VARCHAR(100),
        updated_by VARCHAR(100)
        ) SERVER security_db_server
    OPTIONS (schema_name '@@SECURITY_DB_SCHEMA@@', table_name 'roles');

-- Add user_roles foreign table if not exists
CREATE
    FOREIGN TABLE IF NOT EXISTS remote_user_roles
    (
        user_id UUID NOT NULL,
        role_id UUID NOT NULL,
        status VARCHAR(20) NOT NULL,
        created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITHOUT TIME ZONE,
        deleted_at TIMESTAMP WITHOUT TIME ZONE,
        created_by VARCHAR(100),
        updated_by VARCHAR(100)
        ) SERVER security_db_server
    OPTIONS (schema_name '@@SECURITY_DB_SCHEMA@@', table_name 'user_roles');