DO $$DECLARE
   this_version CONSTANT varchar := '01.01.002';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   -- 01.01.003 replaces this column; do not bring it back on a database that already has terms_version.
   IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'UserTermsAcceptance' AND column_name = 'terms_version') THEN
      ALTER TABLE public."UserTermsAcceptance" ADD COLUMN IF NOT EXISTS content_hash character varying;
   END IF;

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.002', '2026-04-22 12:00:00.000000+02', now(), 'Add content_hash column to UserTermsAcceptance.');

END$$;
