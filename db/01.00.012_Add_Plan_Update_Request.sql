DO $$DECLARE
   this_version CONSTANT varchar := '01.00.012';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

      CREATE TABLE IF NOT EXISTS public."PlanUpdateRequest"
      (
         id uuid NOT NULL,
         plan_id uuid,
         submitter_id uuid NOT NULL,
         action_type smallint NOT NULL,
         status smallint NOT NULL,
         data text COLLATE pg_catalog."default" NOT NULL,
         request_at timestamp without time zone NOT NULL,
         approved_by uuid,
         approved_at timestamp without time zone,
         created_at timestamp without time zone NOT NULL,
         updated_at timestamp without time zone NOT NULL,
         is_active smallint NOT NULL,
         tenant uuid,
         source_code character varying COLLATE pg_catalog."default" NOT NULL,
         CONSTRAINT "PlanUpdateRequest_pkey" PRIMARY KEY (id),
         CONSTRAINT "PlanUpdateRequest_approved_by_fkey" FOREIGN KEY (approved_by)
            REFERENCES public."User" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION,
         CONSTRAINT "PlanUpdateRequest_plan_fkey" FOREIGN KEY (plan_id)
            REFERENCES public."Plan" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION,
         CONSTRAINT "PlanUpdateRequest_submitter_fkey" FOREIGN KEY (submitter_id)
            REFERENCES public."User" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
            NOT VALID,
         CONSTRAINT "PlanUpdateRequest_tenant_fkey" FOREIGN KEY (tenant)
            REFERENCES public."Tenant" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
      );
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.012', '2025-11-07 12:00:00.000000+02', now(), 'Add plan update request table.');

END$$;