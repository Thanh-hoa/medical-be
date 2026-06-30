ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS bhyt_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS citizen_id_hash VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS ux_patients_bhyt_hash
    ON patients (bhyt_hash)
    WHERE bhyt_hash IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_patients_citizen_id_hash
    ON patients (citizen_id_hash)
    WHERE citizen_id_hash IS NOT NULL;
