DO $$DECLARE
   this_version CONSTANT varchar := '01.00.005';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

	CREATE TABLE IF NOT EXISTS public."Evaluation"
	(
		id uuid NOT NULL,
		entity_type smallint NOT NULL,
		entity uuid NOT NULL,
		created_by uuid NOT NULL,
		evaluated_at timestamp without time zone NOT NULL,
		data character varying COLLATE pg_catalog."default" NOT NULL,
		status smallint NOT NULL,
		created_at timestamp without time zone NOT NULL,
		updated_at timestamp without time zone NOT NULL,
		is_active smallint NOT NULL,
		tenant uuid,
		CONSTRAINT "Evaluation_pkey" PRIMARY KEY (id),
		CONSTRAINT "Evaluation_tenant_fkey" FOREIGN KEY (tenant)
			REFERENCES public."Tenant" (id) MATCH SIMPLE
			ON UPDATE NO ACTION
			ON DELETE NO ACTION
			NOT VALID,
		CONSTRAINT "Evaluation_user_fkey" FOREIGN KEY (created_by)
			REFERENCES public."User" (id) MATCH SIMPLE
			ON UPDATE NO ACTION
			ON DELETE NO ACTION
	);
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.005', '2025-01-20 12:00:00.000000+02', now(), 'Add evaluation table.');

END$$;