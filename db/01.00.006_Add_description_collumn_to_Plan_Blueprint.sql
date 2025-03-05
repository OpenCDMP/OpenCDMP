DO $$DECLARE
   this_version CONSTANT varchar := '01.00.006';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

	ALTER TABLE IF EXISTS public."PlanBlueprint"
		ADD COLUMN description text;
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.006', '2025-01-20 12:00:00.000000+02', now(), 'Add description collumn to Plan Blueprint.');

END$$;