ALTER TABLE pairing_code ADD COLUMN organization_id uuid NOT NULL;
ALTER TABLE device ADD COLUMN organization_id uuid NOT NULL;
