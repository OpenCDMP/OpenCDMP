DO $$DECLARE
   this_version CONSTANT varchar := '01.00.013';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   CREATE TABLE IF NOT EXISTS public."PlanBlueprintType"
   (
      id uuid NOT NULL,
      name character varying(250) COLLATE pg_catalog."default" NOT NULL,
      code character varying(200) COLLATE pg_catalog."default" NOT NULL,
      status smallint NOT NULL,
      created_at timestamp without time zone NOT NULL,
      updated_at timestamp without time zone NOT NULL,
      is_active smallint NOT NULL,
      tenant uuid,
      CONSTRAINT "PlanBlueprintType_pkey" PRIMARY KEY (id),
      CONSTRAINT "PlanBlueprintType_tenant_fkey" FOREIGN KEY (tenant)
         REFERENCES public."Tenant" (id) MATCH SIMPLE
         ON UPDATE NO ACTION
         ON DELETE NO ACTION
   );

   ALTER TABLE IF EXISTS public."PlanBlueprint"
      ADD COLUMN type uuid;
   ALTER TABLE IF EXISTS public."PlanBlueprint"
      ADD CONSTRAINT "PlanBlueprint_type_fkey" FOREIGN KEY (type)
      REFERENCES public."PlanBlueprintType" (id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
      NOT VALID;
   
   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.013', '2025-12-18 12:00:00.000000+02', now(), 'Add plan blueprint type table.');

END$$;