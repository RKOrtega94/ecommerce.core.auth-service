SET search_path TO @@DATABASE_SCHEMA@@;

-- Create foreign table for users
CREATE
    FOREIGN TABLE IF NOT EXISTS remote_users
    (
        id UUID NOT NULL,
        firstname VARCHAR(50) NOT NULL,
        lastname VARCHAR(50) NOT NULL,
        username VARCHAR(50) NOT NULL,
        password VARCHAR(255) NOT NULL,
        password_expiration TIMESTAMP WITHOUT TIME ZONE,
        status status_enum  NOT NULL,
        created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITHOUT TIME ZONE,
        deleted_at TIMESTAMP WITHOUT TIME ZONE,
        created_by VARCHAR(100),
        updated_by VARCHAR(100)
        ) SERVER security_db_server
    OPTIONS (schema_name '@@SECURITY_DB_SCHEMA@@', table_name 'users');