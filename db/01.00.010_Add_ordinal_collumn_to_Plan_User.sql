DO $$DECLARE
   this_version CONSTANT varchar := '01.00.010';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

      ALTER TABLE IF EXISTS public."PlanUser"
      ADD COLUMN ordinal integer;

      WITH ranked AS (
         SELECT 
            id,
            plan,
            ROW_NUMBER() OVER (PARTITION BY plan ORDER BY created_at, id) AS new_ordinal
         FROM "PlanUser"
         WHERE ordinal IS NULL
      )

      UPDATE "PlanUser" pu
      SET ordinal = r.new_ordinal
      FROM ranked r
      WHERE pu.id = r.id;

      ALTER TABLE "PlanUser"
      ALTER COLUMN ordinal SET NOT NULL;
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.010', '2025-06-20 12:00:00.000000+02', now(), 'Add ordinal collumn to  Plan User.');

END$$;