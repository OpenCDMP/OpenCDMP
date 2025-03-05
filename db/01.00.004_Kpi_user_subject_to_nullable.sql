DO $$DECLARE
   this_version CONSTANT varchar := '01.00.004';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   ALTER TABLE IF EXISTS public."kpi_user"
      ALTER COLUMN subject_id DROP NOT NULL;
   
   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.004', '2024-09-19 12:00:00.000000+02', now(), 'Make kpi_user subject id nullable.');

END$$;