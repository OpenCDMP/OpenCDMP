DO $$DECLARE
   this_version CONSTANT varchar := '01.01.004';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   ALTER TABLE public."PlanUpdateRequest"
       ADD COLUMN IF NOT EXISTS external_id      text             NULL,
       ADD COLUMN IF NOT EXISTS external_id_type character varying NULL,
       ADD COLUMN IF NOT EXISTS applied_data     text             NULL;

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.004', '2026-06-16 12:00:00.000000+02', now(), 'Add external_id, external_id_type, applied_data to PlanUpdateRequest.');

END$$;
