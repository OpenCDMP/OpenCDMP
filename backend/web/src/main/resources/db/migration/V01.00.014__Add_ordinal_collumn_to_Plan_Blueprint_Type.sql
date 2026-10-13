DO $$DECLARE
   this_version CONSTANT varchar := '01.00.014';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   ALTER TABLE public."PlanBlueprintType"
   ADD COLUMN IF NOT EXISTS ordinal integer;

   WITH numbered AS (
      SELECT 
         id,
         ROW_NUMBER() OVER (ORDER BY created_at, id) AS rn
      FROM public."PlanBlueprintType"
   )
   UPDATE public."PlanBlueprintType" p
   SET ordinal = n.rn
   FROM numbered n
   WHERE p.id = n.id AND p.ordinal IS NULL;

   ALTER TABLE public."PlanBlueprintType"
   ALTER COLUMN ordinal SET NOT NULL;
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.014', '2026-03-26 12:00:00.000000+02', now(), 'Add ordinal collumn to  Plan Blueprint Type.');

END$$;