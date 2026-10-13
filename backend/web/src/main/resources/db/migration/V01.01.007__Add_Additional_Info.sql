DO $$DECLARE
   this_version CONSTANT varchar := '01.01.007';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   ALTER TABLE public."Plan" ADD COLUMN IF NOT EXISTS additional_info text;
   ALTER TABLE public."Description" ADD COLUMN IF NOT EXISTS additional_info text;

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.007', '2026-09-01 12:00:00.000000+02', now(), 'Add additional_info to Plan and Description for server-owned metadata (clone provenance).');

END$$;
