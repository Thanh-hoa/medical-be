ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS account_id BIGINT;

ALTER TABLE patients
    ADD CONSTRAINT fk_patients_account
        FOREIGN KEY (account_id) REFERENCES account(id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_patients_account_id
    ON patients (account_id)
    WHERE account_id IS NOT NULL;
