DO $$DECLARE
   this_version CONSTANT varchar := '01.01.003';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   ALTER TABLE public."UserTermsAcceptance" DROP COLUMN IF EXISTS content_hash;
   ALTER TABLE public."UserTermsAcceptance" ADD COLUMN IF NOT EXISTS terms_version smallint;

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.003', '2026-04-23 12:00:00.000000+02', now(), 'Replace content_hash with terms_version on UserTermsAcceptance.');

END$$;
