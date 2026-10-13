DO $$DECLARE
   this_version CONSTANT varchar := '01.01.005';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   -- Backfill rows created before Phase 2 shipped (external_id was nullable during rollout).
   UPDATE public."PlanUpdateRequest"
      SET external_id = 'legacy:' || id::text
    WHERE external_id IS NULL;

   ALTER TABLE public."PlanUpdateRequest"
       ALTER COLUMN external_id SET NOT NULL;

   CREATE UNIQUE INDEX IF NOT EXISTS "ux_PlanUpdateRequest_pending"
       ON public."PlanUpdateRequest" (tenant, source_code, external_id)
       WHERE status = 0 AND is_active = 1;

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.005', '2026-06-16 12:00:00.000000+02', now(), 'Enforce NOT NULL and unique-pending constraint on PlanUpdateRequest.external_id.');

END$$;
