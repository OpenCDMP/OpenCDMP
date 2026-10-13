DO $$DECLARE
   this_version CONSTANT varchar := '01.01.001';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

  CREATE TABLE IF NOT EXISTS public."UserTermsAcceptance" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    tenant uuid,
    accepted_at timestamp without time zone NOT NULL,
    client_ip character varying,
    CONSTRAINT "UserTermsAcceptance_pkey" PRIMARY KEY (id),
    CONSTRAINT "UserTermsAcceptance_user_fkey" FOREIGN KEY ("user")
        REFERENCES public."User" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT "UserTermsAcceptance_tenant_fkey" FOREIGN KEY (tenant)
        REFERENCES public."Tenant" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
   );
   
   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.001', '2026-04-22 12:00:00.000000+02', now(), 'Add UserTermsAcceptance table.');

END$$;
