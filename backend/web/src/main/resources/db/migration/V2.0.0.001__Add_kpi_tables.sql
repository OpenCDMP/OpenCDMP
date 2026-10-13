-- The KPI tables as a KPI deployment has them (schema taken from a 1.3 installation). Where the KPI service already created them, leave them as they are.
DO $$
BEGIN
   IF to_regclass('public.kpi_user') IS NOT NULL THEN RETURN; END IF;
CREATE TABLE public.kpi_bookmark (
    id uuid NOT NULL,
    name character varying(500) NOT NULL,
    value text NOT NULL,
    user_id uuid NOT NULL,
    type smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL,
    hash_code character varying(200) NOT NULL
);

CREATE TABLE public.kpi_dashboard (
    id uuid NOT NULL,
    key character varying(500) NOT NULL,
    name character varying(500) NOT NULL,
    value text NOT NULL,
    entity_id uuid,
    entity_type smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL
);

CREATE TABLE public.kpi_data_access_request (
    id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    status smallint NOT NULL,
    user_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    config json
);

CREATE TABLE public.kpi_data_group_request (
    id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    status smallint NOT NULL,
    user_id uuid,
    group_hash character varying(500) NOT NULL,
    config json,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL,
    name character varying(500) NOT NULL
);

CREATE TABLE public.kpi_dynamic_page (
    id uuid NOT NULL,
    creator_id uuid,
    visibility smallint NOT NULL,
    "order" integer NOT NULL,
    config json,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    type smallint NOT NULL,
    is_active smallint NOT NULL,
    default_language character varying(50) NOT NULL
);

CREATE TABLE public.kpi_dynamic_page_content (
    id uuid NOT NULL,
    page_id uuid NOT NULL,
    language character varying(50) NOT NULL,
    title character varying(500) NOT NULL,
    content text,
    is_active smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE public.kpi_external_token (
    id uuid NOT NULL,
    token character varying(512) NOT NULL,
    definition json,
    expires_at timestamp with time zone NOT NULL,
    owner_id uuid,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant_id uuid NOT NULL,
    name character varying(1024) NOT NULL,
    type smallint NOT NULL
);

CREATE TABLE public.kpi_indicator (
    id uuid NOT NULL,
    code character varying(200) NOT NULL,
    name character varying(500) NOT NULL,
    description character varying,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL,
    config json
);

CREATE TABLE public.kpi_indicator_access (
    id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    indicator_id uuid NOT NULL,
    user_id uuid,
    config json,
    created_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE public.kpi_queue_inbox (
    id uuid NOT NULL,
    queue character varying(50) NOT NULL,
    exchange character varying(50) NOT NULL,
    route character varying(50) NOT NULL,
    application_id character varying(100) NOT NULL,
    message_id uuid NOT NULL,
    message text NOT NULL,
    retry_count integer,
    tenant_id uuid,
    is_active smallint NOT NULL,
    status smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE public.kpi_queue_outbox (
    id uuid NOT NULL,
    exchange character varying(200) NOT NULL,
    route character varying(200) NOT NULL,
    message_id uuid NOT NULL,
    message text NOT NULL,
    notify_status smallint NOT NULL,
    retry_count integer NOT NULL,
    published_at timestamp with time zone,
    confirmed_at timestamp with time zone,
    tenant_id uuid,
    is_active smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE public.kpi_scheduled_event (
    id uuid NOT NULL,
    key character varying(500) NOT NULL,
    key_type character varying(200) NOT NULL,
    event_type smallint NOT NULL,
    run_at timestamp with time zone NOT NULL,
    creator uuid,
    data json,
    retry_count integer NOT NULL,
    is_active smallint NOT NULL,
    status smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE public.kpi_tenant (
    id uuid NOT NULL,
    name character varying(500) NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    config json,
    code character varying(200) NOT NULL
);

CREATE TABLE public.kpi_tenant_request (
    id uuid NOT NULL,
    message character varying,
    status smallint NOT NULL,
    email character varying(200) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    for_user_id uuid NOT NULL,
    assigned_tenant_id uuid
);

CREATE TABLE public.kpi_tenant_user (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE TABLE public.kpi_user (
    id uuid NOT NULL,
    first_name character varying(200) NOT NULL,
    last_name character varying(200) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL,
    timezone character varying(200) NOT NULL,
    culture character varying(200) NOT NULL,
    language character varying(200) NOT NULL,
    subject_id character varying(150)
);

CREATE TABLE public.kpi_user_contact_info (
    id uuid NOT NULL,
    type smallint NOT NULL,
    value character varying NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    is_active smallint NOT NULL,
    user_id uuid NOT NULL
);

CREATE TABLE public.kpi_user_settings (
    id uuid NOT NULL,
    key character varying(500) NOT NULL,
    entity_type smallint NOT NULL,
    entity_id uuid,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    type smallint NOT NULL,
    value text NOT NULL,
    name character varying(500) NOT NULL
);

CREATE TABLE public.kpi_version_info (
    key character varying(300) NOT NULL,
    version character varying(300) NOT NULL,
    released_at timestamp with time zone NOT NULL,
    deployed_at timestamp with time zone NOT NULL,
    description character varying(300) NOT NULL
);

ALTER TABLE ONLY public.kpi_dashboard
    ADD CONSTRAINT dashboard_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_bookmark
    ADD CONSTRAINT kpi_bookmark_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_data_access_request
    ADD CONSTRAINT kpi_data_access_request_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_data_group_request
    ADD CONSTRAINT kpi_data_group_request_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_dynamic_page_content
    ADD CONSTRAINT kpi_dynamic_page_content_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_dynamic_page
    ADD CONSTRAINT kpi_dynamic_page_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_external_token
    ADD CONSTRAINT kpi_external_token_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_external_token
    ADD CONSTRAINT kpi_external_token_token_key UNIQUE (token);

ALTER TABLE ONLY public.kpi_indicator_access
    ADD CONSTRAINT kpi_indicator_access_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_indicator_access
    ADD CONSTRAINT kpi_indicator_access_tenant_id_indicator_id_user_id_key UNIQUE (tenant_id, indicator_id, user_id);

ALTER TABLE ONLY public.kpi_indicator
    ADD CONSTRAINT kpi_indicator_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_queue_inbox
    ADD CONSTRAINT kpi_queue_inbox_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_queue_outbox
    ADD CONSTRAINT kpi_queue_outbox_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_scheduled_event
    ADD CONSTRAINT kpi_scheduled_event_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_tenant
    ADD CONSTRAINT kpi_tenant_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_tenant_request
    ADD CONSTRAINT kpi_tenant_request_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_tenant_user
    ADD CONSTRAINT kpi_tenant_user_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_user_contact_info
    ADD CONSTRAINT kpi_user_contact_info_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_user
    ADD CONSTRAINT kpi_user_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_user_settings
    ADD CONSTRAINT kpi_user_settings_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.kpi_user
    ADD CONSTRAINT kpi_user_subject_id_key UNIQUE (subject_id);

ALTER TABLE ONLY public.kpi_version_info
    ADD CONSTRAINT kpi_version_info_pkey PRIMARY KEY (key);

ALTER TABLE ONLY public.kpi_bookmark
    ADD CONSTRAINT kpi_bookmark_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.kpi_user(id);

ALTER TABLE ONLY public.kpi_data_access_request
    ADD CONSTRAINT kpi_data_access_request_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.kpi_tenant(id);

ALTER TABLE ONLY public.kpi_data_access_request
    ADD CONSTRAINT kpi_data_access_request_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.kpi_user(id);

ALTER TABLE ONLY public.kpi_data_group_request
    ADD CONSTRAINT kpi_data_group_request_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.kpi_tenant(id);

ALTER TABLE ONLY public.kpi_data_group_request
    ADD CONSTRAINT kpi_data_group_request_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.kpi_user(id) NOT VALID;

ALTER TABLE ONLY public.kpi_dynamic_page_content
    ADD CONSTRAINT kpi_dynamic_page_content_page_id_fkey FOREIGN KEY (page_id) REFERENCES public.kpi_dynamic_page(id) NOT VALID;

ALTER TABLE ONLY public.kpi_dynamic_page
    ADD CONSTRAINT kpi_dynamic_page_creator_id_fke FOREIGN KEY (creator_id) REFERENCES public.kpi_user(id);

ALTER TABLE ONLY public.kpi_external_token
    ADD CONSTRAINT kpi_external_token_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.kpi_user(id);

ALTER TABLE ONLY public.kpi_external_token
    ADD CONSTRAINT kpi_external_token_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.kpi_tenant(id) NOT VALID;

ALTER TABLE ONLY public.kpi_indicator_access
    ADD CONSTRAINT kpi_indicator_access_indicator_id_fkey FOREIGN KEY (indicator_id) REFERENCES public.kpi_indicator(id);

ALTER TABLE ONLY public.kpi_indicator_access
    ADD CONSTRAINT kpi_indicator_access_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.kpi_tenant(id);

ALTER TABLE ONLY public.kpi_indicator_access
    ADD CONSTRAINT kpi_indicator_access_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.kpi_user(id);

ALTER TABLE ONLY public.kpi_queue_outbox
    ADD CONSTRAINT kpi_queue_outbox_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.kpi_tenant(id);

ALTER TABLE ONLY public.kpi_scheduled_event
    ADD CONSTRAINT kpi_scheduled_event_creator_fkey FOREIGN KEY (creator) REFERENCES public.kpi_user(id);

ALTER TABLE ONLY public.kpi_tenant_request
    ADD CONSTRAINT "kpi_tenant_request_assigned_tenant _id_fkey" FOREIGN KEY (assigned_tenant_id) REFERENCES public.kpi_tenant(id) NOT VALID;

ALTER TABLE ONLY public.kpi_tenant_request
    ADD CONSTRAINT kpi_tenant_request_for_user_id_fkey FOREIGN KEY (for_user_id) REFERENCES public.kpi_user(id) NOT VALID;

ALTER TABLE ONLY public.kpi_tenant_user
    ADD CONSTRAINT kpi_tenant_user_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.kpi_tenant(id) NOT VALID;

ALTER TABLE ONLY public.kpi_tenant_user
    ADD CONSTRAINT kpi_tenant_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.kpi_user(id) NOT VALID;

ALTER TABLE ONLY public.kpi_user_contact_info
    ADD CONSTRAINT kpi_user_contact_info_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.kpi_user(id);

END$$;
