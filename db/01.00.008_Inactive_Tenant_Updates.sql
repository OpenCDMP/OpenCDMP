DO $$DECLARE
   this_version CONSTANT varchar := '01.00.008';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

	UPDATE public."TenantUser" tu
   SET is_active = 0
   FROM public."Tenant" t
   WHERE t.id = tu.tenant AND t.is_active = 0 AND tu.is_active = 1;

   UPDATE public."TenantConfiguration" tc
   SET is_active = 0
   FROM public."Tenant" t
   WHERE t.id = tc.tenant AND t.is_active = 0 AND tc.is_active = 1;

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.008', '2025-05-13 12:00:00.000000+02', now(), 'Update TenantUser and TenantConfiguration for inactive tenants');

END$$;