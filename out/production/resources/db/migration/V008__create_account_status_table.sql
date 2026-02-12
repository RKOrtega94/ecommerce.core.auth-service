-- Create account_status_type type
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'account_status_type')
        THEN
            CREATE TYPE account_status_type AS ENUM ('DISABLED', 'LOCKED');
        END IF;
    END;
$$;

SET search_path TO @@DATABASE_SCHEMA@@;

-- Create the account_status table
CREATE TABLE IF NOT EXISTS account_status
(
    ID         UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    USER_ID    UUID                NOT NULL UNIQUE,
    STATUS     account_status_type NOT NULL,
    REASON     TEXT,
    CREATED_AT TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    EXPIRES_AT TIMESTAMPTZ
);

-- Create indexes for account_status table
CREATE INDEX IF NOT EXISTS idx_account_status_user_id ON account_status (USER_ID);
CREATE INDEX IF NOT EXISTS idx_account_status_status ON account_status (STATUS);
CREATE INDEX IF NOT EXISTS idx_account_status_expires_at ON account_status (EXPIRES_AT);

-- Validate against foreign table (FKs cannot reference foreign tables).
CREATE OR REPLACE FUNCTION validate_account_status_user_id()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    IF NEW.user_id IS NULL OR NOT EXISTS (
        SELECT 1
        FROM remote_users
        WHERE id = NEW.user_id
    ) THEN
        RAISE EXCEPTION 'Invalid credentials';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validate_account_status_user_id
    BEFORE INSERT OR UPDATE OF user_id ON account_status
    FOR EACH ROW
    EXECUTE FUNCTION validate_account_status_user_id();

-- Cleanup helper; schedule weekly at 2am in a future migration (pg_cron).
CREATE OR REPLACE FUNCTION cleanup_orphaned_account_status()
    RETURNS INTEGER
    LANGUAGE plpgsql AS
$$
DECLARE
    v_deleted INTEGER;
BEGIN
    DELETE FROM account_status a
    WHERE NOT EXISTS (
        SELECT 1
        FROM remote_users r
        WHERE r.id = a.user_id
    );

    GET DIAGNOSTICS v_deleted = ROW_COUNT;
    RETURN v_deleted;
END;
$$;
