DO $$DECLARE
   this_version CONSTANT varchar := '01.00.011';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

      CREATE TABLE IF NOT EXISTS public."madmp_User"
      (
         id uuid NOT NULL,
         name character varying(250) COLLATE pg_catalog."default",
         additional_info character varying COLLATE pg_catalog."default",
         created_at timestamp without time zone NOT NULL DEFAULT now(),
         updated_at timestamp without time zone NOT NULL DEFAULT now(),
         is_active smallint NOT NULL DEFAULT 1,
         CONSTRAINT "madmp_User_pkey" PRIMARY KEY (id)
      );

      CREATE TABLE IF NOT EXISTS public."madmp_UserCredential"
      (
         id uuid NOT NULL,
         "user" uuid NOT NULL,
         external_id character varying(512) COLLATE pg_catalog."default" NOT NULL,
         created_at timestamp without time zone NOT NULL,
         updated_at timestamp without time zone NOT NULL,
         data character varying COLLATE pg_catalog."default",
         CONSTRAINT "madmp_UserCredential_pkey" PRIMARY KEY (id),
         CONSTRAINT "madmp_UserCredential_user_fkey" FOREIGN KEY ("user")
            REFERENCES public."madmp_User" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
      );

      CREATE TABLE IF NOT EXISTS public."madmp_Tenant"
      (
         id uuid NOT NULL,
         code character varying(200) COLLATE pg_catalog."default" NOT NULL,
         created_at timestamp without time zone NOT NULL DEFAULT now(),
         updated_at timestamp without time zone NOT NULL DEFAULT now(),
         is_active smallint NOT NULL DEFAULT 1,
         CONSTRAINT "madmp_Tenant_pkey" PRIMARY KEY (id)
      );

      CREATE TABLE IF NOT EXISTS public."madmp_TenantUser"
      (
         id uuid NOT NULL,
         "user" uuid NOT NULL,
         tenant uuid NOT NULL,
         is_active smallint NOT NULL,
         created_at timestamp without time zone NOT NULL,
         updated_at timestamp without time zone NOT NULL,
         CONSTRAINT "madmp_TenantUser_pkey" PRIMARY KEY (id),
         CONSTRAINT "madmp_TenantUser_tenant_fkey" FOREIGN KEY (tenant)
            REFERENCES public."madmp_Tenant" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION,
         CONSTRAINT "madmp_TenantUser_user_fkey" FOREIGN KEY ("user")
            REFERENCES public."madmp_User" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
      );

      CREATE TABLE IF NOT EXISTS public."madmp_QueueInbox"
      (
         id uuid NOT NULL,
         queue character varying(200) COLLATE pg_catalog."default" NOT NULL,
         exchange character varying(200) COLLATE pg_catalog."default" NOT NULL,
         route character varying(200) COLLATE pg_catalog."default" NOT NULL,
         application_id character varying(100) COLLATE pg_catalog."default" NOT NULL,
         message_id uuid NOT NULL,
         message json NOT NULL,
         retry_count integer,
         status smallint NOT NULL,
         created_at timestamp without time zone NOT NULL,
         updated_at timestamp without time zone NOT NULL,
         tenant uuid,
         is_active smallint NOT NULL,
         CONSTRAINT "madmp_QueryInbox_pkey" PRIMARY KEY (id),
         CONSTRAINT "madmp_QueryInbox_tenant_fkey" FOREIGN KEY (tenant)
            REFERENCES public."ant_Tenant" (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
      );

      CREATE TABLE IF NOT EXISTS public."madmp_QueueOutbox"
      (
         id uuid NOT NULL,
         exchange character varying(200) COLLATE pg_catalog."default" NOT NULL,
         route character varying(200) COLLATE pg_catalog."default" NOT NULL,
         message_id uuid NOT NULL,
         notify_status smallint NOT NULL,
         retry_count integer NOT NULL,
         published_at timestamp without time zone,
         confirmed_at timestamp without time zone,
         tenant uuid,
         created_at timestamp without time zone NOT NULL,
         updated_at timestamp without time zone NOT NULL,
         message text COLLATE pg_catalog."default" NOT NULL,
         is_active smallint NOT NULL,
         CONSTRAINT "madmp_QueueOutbox_pkey" PRIMARY KEY (id)
      );
   
    INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.011', '2025-09-19 12:00:00.000000+02', now(), 'Add madmp tables.');

END$$;