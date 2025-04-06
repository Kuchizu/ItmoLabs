CREATE OR REPLACE PROCEDURE drop_null(schema_name TEXT)
LANGUAGE plpgsql
AS
$$
DECLARE
    v_schema TEXT := schema_name;
    rec RECORD;
    constraints_disabled INT := 0;
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_catalog.pg_namespace
        WHERE nspname = v_schema
    ) THEN
        RAISE NOTICE 'Schema "%" does not exist.', v_schema;
        RETURN;
    END IF;

    IF NOT has_schema_privilege(v_schema, 'USAGE') THEN
        RAISE NOTICE 'Insufficient privileges: not enough priviliges to edit schema "%".', v_schema;
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_catalog.pg_namespace ns
        JOIN pg_catalog.pg_class c ON ns.oid = c.relnamespace
        JOIN pg_catalog.pg_attribute a ON c.oid = a.attrelid
        WHERE ns.nspname = v_schema
          AND c.relkind IN ('r','p')
          AND a.attnum > 0
          AND NOT a.attisdropped
          AND a.attnotnull = TRUE
          AND NOT has_table_privilege(c.oid::regclass, 'UPDATE')
    ) THEN
        RAISE NOTICE 'Insufficient privileges: current user cannot update all tables in schema "%".', v_schema;
        RETURN;
    END IF;

    FOR rec IN
        SELECT ns.nspname   AS schema_name,
               c.relname    AS table_name,
               a.attname    AS column_name
        FROM pg_catalog.pg_namespace ns
        JOIN pg_catalog.pg_class c
            ON ns.oid = c.relnamespace
        JOIN pg_catalog.pg_attribute a
            ON c.oid = a.attrelid
        WHERE ns.nspname = v_schema
          AND c.relkind IN ('r','p')
          AND a.attnum > 0
          AND NOT a.attisdropped
          AND a.attnotnull = TRUE
          AND NOT EXISTS (
                SELECT 1 FROM pg_catalog.pg_constraint co
                WHERE co.conrelid = c.oid
                  AND co.contype = 'p'
                  AND a.attnum = ANY(co.conkey)
            )
    LOOP
        EXECUTE format(
            'ALTER TABLE %I.%I ALTER COLUMN %I DROP NOT NULL',
            rec.schema_name, rec.table_name, rec.column_name
        );
        constraints_disabled := constraints_disabled + 1;
    END LOOP;

    RAISE NOTICE 'Schema: %', v_schema;
    RAISE NOTICE 'Disabled NOT NULL constraints: %', constraints_disabled;
END;
$$;

CALL drop_null(:'schema_name');