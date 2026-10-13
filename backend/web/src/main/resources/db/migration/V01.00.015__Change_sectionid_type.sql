DO $$DECLARE
   this_version CONSTANT varchar := '01.00.015';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   -- PlanDescriptionTemplate
   ALTER TABLE public."PlanDescriptionTemplate"
      ALTER COLUMN section_id TYPE text
      USING section_id::text;


   -- PlanUser
   ALTER TABLE public."PlanUser"
      ALTER COLUMN section_id TYPE text
      USING section_id::text;
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.015', '2026-05-20 12:00:00.000000+02', now(), 'Change section id type from UUID to String.');

END$$;