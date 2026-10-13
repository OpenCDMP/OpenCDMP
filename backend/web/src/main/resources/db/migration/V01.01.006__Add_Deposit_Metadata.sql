DO $$DECLARE
   this_version CONSTANT varchar := '01.01.006';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

  CREATE TABLE IF NOT EXISTS public."DepositMetadata" (
    id uuid NOT NULL,
    plan uuid NOT NULL,
    repository_id character varying NOT NULL,
    payload text NOT NULL,
    tenant uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    CONSTRAINT "DepositMetadata_pkey" PRIMARY KEY (id),
    CONSTRAINT "DepositMetadata_plan_fkey" FOREIGN KEY (plan)
        REFERENCES public."Plan" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT "DepositMetadata_tenant_fkey" FOREIGN KEY (tenant)
        REFERENCES public."Tenant" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
   );

   CREATE INDEX IF NOT EXISTS "DepositMetadata_plan_repository_idx" ON public."DepositMetadata" (plan, repository_id);

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.006', '2026-07-22 12:00:00.000000+02', now(), 'Add DepositMetadata table for persisted deposit preview overrides.');

END$$;
