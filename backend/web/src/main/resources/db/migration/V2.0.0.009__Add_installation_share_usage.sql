ALTER TABLE public."Installation"
    ADD COLUMN IF NOT EXISTS share_usage boolean NOT NULL DEFAULT true;
