DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    FOR constraint_record IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_attribute att
            ON att.attrelid = con.conrelid
            AND att.attnum = ANY(con.conkey)
        WHERE con.conrelid = 'patients'::regclass
            AND con.contype = 'u'
        GROUP BY con.conname
        HAVING array_agg(att.attname ORDER BY att.attnum) = ARRAY['bhyt']::name[]
    LOOP
        EXECUTE format('ALTER TABLE patients DROP CONSTRAINT %I', constraint_record.conname);
    END LOOP;
END $$;
