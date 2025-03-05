DO $$DECLARE
   this_version CONSTANT varchar := '01.00.001';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

	ALTER TABLE IF EXISTS public."PlanStatus"	
    	ADD COLUMN action character varying(250);

	ALTER TABLE IF EXISTS public."PlanStatus"
    	ADD COLUMN ordinal integer NOT NULL;

	ALTER TABLE IF EXISTS public."DescriptionStatus"
    	ADD COLUMN action character varying(250);

	ALTER TABLE IF EXISTS public."DescriptionStatus"
    	ADD COLUMN ordinal integer NOT NULL;
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.001', '2024-08-21 12:00:00.000000+02', now(), 'Add action, ordinal columns on plan, description status table.');

END$$;