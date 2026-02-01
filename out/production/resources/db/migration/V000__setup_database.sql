-- ============================================================================
-- SISTEMA DE AUDITORÍA AUTOMÁTICA PARA POSTGRESQL 16+
-- Versión Simplificada y Funcional
-- ============================================================================

-- ----------------------------------------------------------------------------
-- SET DEFAULT SCHEMA @@DATABASE_SCHEMA@@
-- ----------------------------------------------------------------------------
SET
search_path TO @@DATABASE_SCHEMA@@;

-- ----------------------------------------------------------------------------
-- PASO 1: FUNCIONES TRIGGER PARA TIMESTAMPS
-- ----------------------------------------------------------------------------
CREATE
OR REPLACE FUNCTION trigger_set_timestamps()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
DECLARE
C_OP_INSERT CONSTANT TEXT := 'INSERT';
    C_OP_UPDATE
CONSTANT TEXT := 'UPDATE';
BEGIN
    IF
TG_OP = C_OP_INSERT THEN
        NEW.created_at := CURRENT_TIMESTAMP;
        NEW.updated_at
:= CURRENT_TIMESTAMP;
    ELSIF
TG_OP = C_OP_UPDATE THEN
        NEW.created_at := OLD.created_at;
        NEW.updated_at
:= CURRENT_TIMESTAMP;
END IF;
RETURN NEW;
END;
$$;

CREATE
OR REPLACE FUNCTION trigger_set_deleted_at()
    RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
DECLARE
C_DELETED CONSTANT TEXT := 'DELETED';
BEGIN
    IF
NEW.status = C_DELETED AND (OLD.status IS NULL OR OLD.status != C_DELETED) THEN
        NEW.deleted_at := CURRENT_TIMESTAMP;
    ELSIF
NEW.status != C_DELETED AND OLD.status = C_DELETED THEN
        NEW.deleted_at := NULL;
END IF;
RETURN NEW;
END;
$$;

-- ----------------------------------------------------------------------------
-- PASO 2: FUNCIÓN PARA AGREGAR COLUMNAS Y TRIGGERS
-- ----------------------------------------------------------------------------
CREATE
OR REPLACE FUNCTION setup_audit_for_table(
    p_schema_name TEXT,
    p_table_name TEXT
)
    RETURNS VOID
    LANGUAGE plpgsql AS
$$
DECLARE
v_full_table TEXT;
    v_sql
TEXT;
    -- Constants for column names
    C_STATUS
CONSTANT TEXT := 'status';
    C_CREATED_AT
CONSTANT TEXT := 'created_at';
    C_UPDATED_AT
CONSTANT TEXT := 'updated_at';
    C_DELETED_AT
CONSTANT TEXT := 'deleted_at';
    C_CREATED_BY
CONSTANT TEXT := 'created_by';
    C_UPDATED_BY
CONSTANT TEXT := 'updated_by';

    -- Constants for values
    C_VAL_ACTIVE
CONSTANT TEXT := 'ACTIVE';
    C_VAL_INACTIVE
CONSTANT TEXT := 'INACTIVE';
    C_VAL_DELETED
CONSTANT TEXT := 'DELETED';
BEGIN
    v_full_table
:= quote_ident(p_schema_name) || '.' || quote_ident(p_table_name);

    RAISE
NOTICE '===> Procesando tabla: %', v_full_table;

    -- Agregar STATUS si no existe
    IF
NOT EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = p_schema_name
                     AND table_name = p_table_name
                     AND column_name = C_STATUS) THEN
        v_sql := format(
                'ALTER TABLE %s ADD COLUMN %I VARCHAR(10) DEFAULT %L
                 CHECK (%I IN (%L, %L, %L))',
                v_full_table, C_STATUS, C_VAL_ACTIVE, C_STATUS, C_VAL_ACTIVE, C_VAL_INACTIVE, C_VAL_DELETED
                 );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Columna % agregada', C_STATUS;
END IF;

    -- Agregar CREATED_AT si no existe
    IF
NOT EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = p_schema_name
                     AND table_name = p_table_name
                     AND column_name = C_CREATED_AT) THEN
        v_sql := format(
                'ALTER TABLE %s ADD COLUMN %I TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
                v_full_table, C_CREATED_AT
                 );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Columna % agregada', C_CREATED_AT;
END IF;

    -- Agregar UPDATED_AT si no existe
    IF
NOT EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = p_schema_name
                     AND table_name = p_table_name
                     AND column_name = C_UPDATED_AT) THEN
        v_sql := format(
                'ALTER TABLE %s ADD COLUMN %I TIMESTAMP DEFAULT NOW()',
                v_full_table, C_UPDATED_AT
                 );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Columna % agregada', C_UPDATED_AT;
END IF;

    -- Agregar DELETED_AT si no existe
    IF
NOT EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = p_schema_name
                     AND table_name = p_table_name
                     AND column_name = C_DELETED_AT) THEN
        v_sql := format(
                'ALTER TABLE %s ADD COLUMN %I TIMESTAMP',
                v_full_table, C_DELETED_AT
                 );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Columna % agregada', C_DELETED_AT;
END IF;

    -- Agregar CREATED_BY si no existe
    IF
NOT EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = p_schema_name
                     AND table_name = p_table_name
                     AND column_name = C_CREATED_BY) THEN
        v_sql := format(
                'ALTER TABLE %s ADD COLUMN %I VARCHAR(100)',
                v_full_table, C_CREATED_BY
                 );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Columna % agregada', C_CREATED_BY;
END IF;

    -- Agregar UPDATED_BY si no existe
    IF
NOT EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = p_schema_name
                     AND table_name = p_table_name
                     AND column_name = C_UPDATED_BY) THEN
        v_sql := format(
                'ALTER TABLE %s ADD COLUMN %I VARCHAR(100)',
                v_full_table, C_UPDATED_BY
                 );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Columna % agregada', C_UPDATED_BY;
END IF;

    -- Crear trigger para timestamps
    v_sql
:= format('DROP TRIGGER IF EXISTS trg_%s_timestamps ON %s', p_table_name, v_full_table);
EXECUTE v_sql;

v_sql
:= format(
            'CREATE TRIGGER trg_%s_timestamps
             BEFORE INSERT OR UPDATE ON %s
             FOR EACH ROW
             EXECUTE FUNCTION trigger_set_timestamps()',
            p_table_name, v_full_table
             );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Trigger timestamps creado';

    -- Crear trigger para deleted_at
    v_sql
:= format('DROP TRIGGER IF EXISTS trg_%s_deleted_at ON %s', p_table_name, v_full_table);
EXECUTE v_sql;

v_sql
:= format(
            'CREATE TRIGGER trg_%s_deleted_at
             BEFORE UPDATE ON %s
             FOR EACH ROW
             EXECUTE FUNCTION trigger_set_deleted_at()',
            p_table_name, v_full_table
             );
EXECUTE v_sql;
RAISE
NOTICE '  ✓ Trigger deleted_at creado';

    RAISE
NOTICE '===> Auditoría completada para: %', v_full_table;
END;
$$;

-- ----------------------------------------------------------------------------
-- PASO 3: EVENT TRIGGER PRINCIPAL
-- ----------------------------------------------------------------------------
CREATE
OR REPLACE FUNCTION event_trigger_auto_audit()
    RETURNS event_trigger
    LANGUAGE plpgsql AS
$$
DECLARE
v_obj    RECORD;
    v_schema
TEXT;
    v_table
TEXT;
    -- Constants for exclusions
    C_PG_CATALOG
CONSTANT TEXT := 'pg_catalog';
    C_INFO_SCHEMA
CONSTANT TEXT := 'information_schema';
    C_PG_TOAST
CONSTANT TEXT := 'pg_toast';
    C_PG_PATTERN
CONSTANT TEXT := 'pg_%';
    C_SQL_PATTERN
CONSTANT TEXT := 'sql_%';
BEGIN
    RAISE
NOTICE '>>> Event Trigger ejecutándose...';

FOR v_obj IN
SELECT *
FROM pg_event_trigger_ddl_commands() LOOP RAISE NOTICE '>>> Objeto detectado: type=%, identity=%',
                v_obj.object_type, v_obj.object_identity;

IF
v_obj.object_type = 'table' THEN
                v_schema := v_obj.schema_name;

                -- Extraer nombre de tabla del object_identity
                -- Formato puede ser: "schema"."table" o solo "table"
                IF
v_obj.object_identity LIKE '%.%' THEN
                    v_table := regexp_replace(v_obj.object_identity, '^.*\.', '');
ELSE
                    v_table := v_obj.object_identity;
END IF;

                -- Limpiar comillas
                v_schema
:= replace(v_schema, '"', '');
                v_table
:= replace(v_table, '"', '');

                RAISE
NOTICE '>>> Extraído: schema=%, table=%', v_schema, v_table;

                -- Excluir schemas del sistema
                IF
v_schema NOT IN (C_PG_CATALOG, C_INFO_SCHEMA, C_PG_TOAST)
                    AND v_table NOT LIKE C_PG_PATTERN
                    AND v_table NOT LIKE C_SQL_PATTERN THEN

BEGIN
                        RAISE
NOTICE '>>> Aplicando auditoría a %.%', v_schema, v_table;
                        PERFORM
setup_audit_for_table(v_schema, v_table);
EXCEPTION
                        WHEN OTHERS THEN
                            RAISE WARNING '>>> ERROR en %.%: %', v_schema, v_table, SQLERRM;
END;
ELSE
                    RAISE NOTICE '>>> Tabla excluida: %.%', v_schema, v_table;
END IF;
END IF;
END LOOP;

    RAISE
NOTICE '>>> Event Trigger finalizado';
END;
$$;

-- Eliminar event trigger anterior
DROP
EVENT TRIGGER IF EXISTS auto_audit_on_create_table;

-- Crear event trigger
CREATE
EVENT TRIGGER auto_audit_on_create_table
    ON ddl_command_end
    WHEN TAG IN ('CREATE TABLE')
EXECUTE FUNCTION event_trigger_auto_audit();

-- Habilitar
    ALTER
EVENT TRIGGER auto_audit_on_create_table ENABLE;

-- ----------------------------------------------------------------------------
-- FUNCIONES DE UTILIDAD
-- ----------------------------------------------------------------------------

-- Verificar estado
CREATE
OR REPLACE FUNCTION check_auto_audit_status()
    RETURNS TABLE
            (
                trigger_name TEXT,
                is_enabled   TEXT,
                event        TEXT,
                tags         TEXT[]
            )
    LANGUAGE SQL
AS
$$
SELECT evtname::TEXT, CASE evtenabled
                          WHEN 'O' THEN 'HABILITADO ✓'
                          WHEN 'D' THEN 'DESHABILITADO ✗'
                          ELSE 'DESCONOCIDO: ' || evtenabled::TEXT
    END,
       evtevent::TEXT, evttags
FROM pg_event_trigger
WHERE evtname = 'auto_audit_on_create_table';
$$;

-- Habilitar/Deshabilitar
CREATE
OR REPLACE FUNCTION enable_auto_audit()
    RETURNS TEXT
    LANGUAGE plpgsql AS
$$
BEGIN
    ALTER
EVENT TRIGGER auto_audit_on_create_table ENABLE;
RETURN '✓ Auditoría automática HABILITADA';
END;
$$;

CREATE
OR REPLACE FUNCTION disable_auto_audit()
    RETURNS TEXT
    LANGUAGE plpgsql AS
$$
BEGIN
    ALTER
EVENT TRIGGER auto_audit_on_create_table DISABLE;
RETURN '✗ Auditoría automática DESHABILITADA';
END;
$$;

-- Aplicar a tabla específica manualmente
CREATE
OR REPLACE FUNCTION apply_audit_to_table(
    p_table_name TEXT,
    p_schema_name TEXT DEFAULT '@@DATABASE_SCHEMA@@'
)
    RETURNS TEXT
    LANGUAGE plpgsql AS
$$
BEGIN
    PERFORM
setup_audit_for_table(p_schema_name, p_table_name);
RETURN format('✓ Auditoría aplicada a %.%', p_schema_name, p_table_name);
EXCEPTION
    WHEN OTHERS THEN
        RETURN format('✗ Error: %', SQLERRM);
END;
$$;

-- Aplicar a todas las tablas existentes
CREATE
OR REPLACE FUNCTION apply_audit_to_all_tables(
    p_schema_name TEXT DEFAULT '@@DATABASE_SCHEMA@@'
)
    RETURNS TABLE
            (
                table_name TEXT,
                status     TEXT
            )
    LANGUAGE plpgsql
AS
$$
DECLARE
v_table RECORD;
    -- Constants for filters
    C_BASE_TABLE
CONSTANT TEXT := 'BASE TABLE';
    C_PG_PATTERN
CONSTANT TEXT := 'pg_%';
    C_SQL_PATTERN
CONSTANT TEXT := 'sql_%';
BEGIN
FOR v_table IN
SELECT t.table_name
FROM information_schema.tables t
WHERE t.table_schema = p_schema_name
  AND t.table_type = C_BASE_TABLE
  AND t.table_name NOT LIKE C_PG_PATTERN
  AND t.table_name NOT LIKE C_SQL_PATTERN
ORDER BY t.table_name
    LOOP
BEGIN
                PERFORM
setup_audit_for_table(p_schema_name, v_table.table_name);
                table_name
:= v_table.table_name;
                status
:= '✓ OK';
                RETURN
NEXT;
EXCEPTION
                WHEN OTHERS THEN
                    table_name := v_table.table_name;
                    status
:= '✗ ERROR: ' || SQLERRM;
                    RETURN
NEXT;
END;
END LOOP;
END;
$$;

-- ============================================================================
-- VERIFICACIÓN Y PRUEBAS
-- ============================================================================

-- 1. Verificar que el event trigger está instalado
SELECT *
FROM check_auto_audit_status();

-- 2. Ver todos los event triggers del sistema
SELECT evtname,
       evtevent,
       evtenabled,
       evttags,
       evtowner::regrole
FROM pg_event_trigger
ORDER BY evtname;

-- ============================================================================
-- PRUEBA RÁPIDA (Descomenta para ejecutar)
-- ============================================================================

/*
-- Deshabilita temporalmente para probar manualmente
SELECT disable_auto_audit();

-- Crea una tabla de prueba
CREATE TABLE test_audit_demo (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    descripcion TEXT
);

-- Aplica auditoría manualmente
SELECT apply_audit_to_table('test_audit_demo');

-- Verifica las columnas
SELECT
    column_name,
    data_type,
    column_default,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'test_audit_demo'
ORDER BY ordinal_position;

-- Verifica los triggers
SELECT
    trigger_name,
    event_manipulation,
    action_statement
FROM information_schema.triggers
WHERE event_object_table = 'test_audit_demo';

-- Prueba insertar datos
INSERT INTO test_audit_demo (nombre, descripcion)
VALUES ('Producto 1', 'Descripción de prueba');

-- Verifica que los timestamps se crearon automáticamente
SELECT * FROM test_audit_demo;

-- Prueba actualizar
UPDATE test_audit_demo
SET nombre = 'Producto Modificado'
WHERE id = 1;

-- Verifica que updated_at cambió
SELECT id, nombre, created_at, updated_at FROM test_audit_demo;

-- Prueba soft delete
UPDATE test_audit_demo
SET status = 'DELETED'
WHERE id = 1;

-- Verifica que deleted_at se estableció
SELECT id, nombre, status, deleted_at FROM test_audit_demo;

-- Limpieza
DROP TABLE test_audit_demo;

-- Rehabilita el event trigger
SELECT enable_auto_audit();
*/

-- ============================================================================
-- INSTRUCCIONES DE USO
-- ============================================================================

-- PARA PROBAR QUE FUNCIONA AUTOMÁTICAMENTE:
-- 1. Verifica que esté habilitado:
--    SELECT * FROM check_auto_audit_status();
--
-- 2. Crea cualquier tabla nueva:
--    CREATE TABLE mi_tabla (id SERIAL, nombre TEXT);
--
-- 3. Verifica que las columnas se agregaron:
--    \d mi_tabla
--    o
--    SELECT column_name FROM information_schema.columns
--    WHERE table_name = 'mi_tabla';
--
-- PARA APLICAR A TABLAS EXISTENTES:
--    SELECT * FROM apply_audit_to_all_tables('@@DATABASE_SCHEMA@@');
--
-- PARA HABILITAR/DESHABILITAR:
--    SELECT enable_auto_audit();
--    SELECT disable_auto_audit();

-- ============================================================================
-- NOTAS IMPORTANTES
-- ============================================================================
-- ✓ Los RAISE NOTICE te ayudarán a ver qué está pasando en tiempo real
-- ✓ Verifica los mensajes en la consola cuando crees una tabla
-- ✓ Si no ves mensajes, el event trigger no se está ejecutando
-- ✓ Requiere PostgreSQL 16+ (para event triggers)
-- ✓ El usuario debe tener permisos de superusuario o database owner
-- ============================================================================