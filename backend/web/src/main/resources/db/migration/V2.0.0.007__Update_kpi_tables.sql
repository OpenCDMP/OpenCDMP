-- KPI viewer (sti-viewer) db scripts 02.03.001-02.05.001, for installs whose kpi_ tables predate them.
DO $$
DECLARE
   col record;
BEGIN
   IF to_regclass('public.kpi_user') IS NULL THEN RETURN; END IF;

   ALTER TABLE public.kpi_queue_inbox DROP CONSTRAINT IF EXISTS kpi_queue_inbox_tenant_id_fkey;

   CREATE TABLE IF NOT EXISTS public.kpi_dashboard (
       id uuid NOT NULL,
       key character varying(500) NOT NULL,
       name character varying(500) NOT NULL,
       value text NOT NULL,
       entity_id uuid,
       entity_type smallint NOT NULL,
       created_at timestamp with time zone NOT NULL,
       updated_at timestamp with time zone NOT NULL,
       is_active smallint NOT NULL,
       CONSTRAINT dashboard_pkey PRIMARY KEY (id)
   );

   INSERT INTO public.kpi_dashboard (id, key, name, value, entity_id, entity_type, created_at, updated_at, is_active)
   SELECT id, key, name, value, entity_id, entity_type, created_at AT TIME ZONE 'UTC', updated_at AT TIME ZONE 'UTC', 1
   FROM public.kpi_user_settings
   WHERE type = 2
   ON CONFLICT (id) DO NOTHING;

   DELETE FROM public.kpi_user_settings WHERE type = 2;

   FOR col IN
      SELECT table_name, column_name FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name LIKE 'kpi\_%' AND data_type = 'timestamp without time zone'
   LOOP
      EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I TYPE timestamp with time zone USING %I AT TIME ZONE ''UTC''', col.table_name, col.column_name, col.column_name);
   END LOOP;

   FOR col IN
      SELECT table_name, column_name FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name IN ('kpi_indicator', 'kpi_indicator_access', 'kpi_tenant')
        AND column_name = 'config' AND data_type = 'character varying'
   LOOP
      EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I TYPE json USING %I::json', col.table_name, col.column_name, col.column_name);
   END LOOP;
END$$;
