ALTER TABLE public."EntityDoi"
    ADD COLUMN IF NOT EXISTS status smallint NOT NULL DEFAULT 1;

ALTER TABLE public."EntityDoi"
    ALTER COLUMN doi DROP NOT NULL;

-- one deposit of a plan to a repository at a time: a second claim fails here while the first is pending
CREATE UNIQUE INDEX IF NOT EXISTS "EntityDoi_entity_id_repository_id_pending_idx" ON public."EntityDoi" (entity_id, repository_id) WHERE status = 0 AND is_active = 1;
