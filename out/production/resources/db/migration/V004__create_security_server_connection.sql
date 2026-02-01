SET search_path TO @@DATABASE_SCHEMA@@;

-- Add the postgres_fdw extension to enable foreign data wrapper functionality
CREATE
EXTENSION IF NOT EXISTS postgres_fdw;

-- Create security_server_connection
CREATE
SERVER IF NOT EXISTS security_db_server
    FOREIGN DATA WRAPPER postgres_fdw
    OPTIONS (
        host '@@SECURITY_DB_HOST@@',
        port '@@SECURITY_DB_PORT@@',
        dbname '@@SECURITY_DB_NAME@@'
    );

-- Create user mapping for the foreign server
CREATE
USER MAPPING IF NOT EXISTS FOR CURRENT_USER
    SERVER security_db_server
    OPTIONS (
        user '@@SECURITY_DB_USER@@',
        password '@@SECURITY_DB_PASSWORD@@'
    );