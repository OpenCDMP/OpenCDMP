DO $$DECLARE
   this_version CONSTANT varchar := '01.01.009';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   CREATE TABLE IF NOT EXISTS public."WebhookSubscription" (
      id uuid NOT NULL,
      label character varying NOT NULL,
      url character varying NOT NULL,
      secret character varying NOT NULL,
      event_types character varying NOT NULL,
      enabled boolean NOT NULL,
      tenant uuid,
      created_at timestamp without time zone NOT NULL,
      updated_at timestamp without time zone NOT NULL,
      is_active smallint NOT NULL,
      CONSTRAINT "WebhookSubscription_pkey" PRIMARY KEY (id),
      CONSTRAINT "WebhookSubscription_tenant_fkey" FOREIGN KEY (tenant)
          REFERENCES public."Tenant" (id) MATCH SIMPLE
          ON UPDATE NO ACTION
          ON DELETE NO ACTION
          NOT VALID
   );

   CREATE TABLE IF NOT EXISTS public."WebhookDelivery" (
      id uuid NOT NULL,
      subscription uuid NOT NULL,
      event_type smallint NOT NULL,
      payload text NOT NULL,
      status smallint NOT NULL,
      attempts integer NOT NULL,
      last_http_status integer,
      last_error character varying,
      next_retry_at timestamp without time zone,
      delivered_at timestamp without time zone,
      tenant uuid,
      created_at timestamp without time zone NOT NULL,
      updated_at timestamp without time zone NOT NULL,
      is_active smallint NOT NULL,
      CONSTRAINT "WebhookDelivery_pkey" PRIMARY KEY (id),
      CONSTRAINT "WebhookDelivery_subscription_fkey" FOREIGN KEY (subscription)
          REFERENCES public."WebhookSubscription" (id) MATCH SIMPLE
          ON UPDATE NO ACTION
          ON DELETE NO ACTION
          NOT VALID,
      CONSTRAINT "WebhookDelivery_tenant_fkey" FOREIGN KEY (tenant)
          REFERENCES public."Tenant" (id) MATCH SIMPLE
          ON UPDATE NO ACTION
          ON DELETE NO ACTION
          NOT VALID
   );

   CREATE INDEX IF NOT EXISTS "WebhookDelivery_worker_idx" ON public."WebhookDelivery" (status, next_retry_at);
   CREATE INDEX IF NOT EXISTS "WebhookDelivery_subscription_idx" ON public."WebhookDelivery" (subscription, created_at);

   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.01.009', '2026-09-01 13:00:00.000000+02', now(), 'Add WebhookSubscription and WebhookDelivery tables for tenant-configurable webhooks.');

END$$;
