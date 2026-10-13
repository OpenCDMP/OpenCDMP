-- OpenCDMP 1.2 schema (01.00.000 seed + db/01.00.001-010) plus the fresh-install seed rows, generated with pg_dump. Do not edit: V01_00_010__Baseline runs it only on an empty database.
--
-- PostgreSQL database dump
--


-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: doientitytype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.doientitytype AS ENUM (
    'DMP'
);


--
-- Name: entitytype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.entitytype AS ENUM (
    'DATASET',
    'DMP'
);




--
-- Name: ActionConfirmation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ActionConfirmation" (
    id uuid NOT NULL,
    type smallint NOT NULL,
    status smallint NOT NULL,
    token character varying NOT NULL,
    data text NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    created_by uuid NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid
);


--
-- Name: DBVersion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."DBVersion" (
    key character varying NOT NULL,
    version character varying NOT NULL,
    "releasedAt" timestamp with time zone NOT NULL,
    "deployedAt" timestamp with time zone,
    description text
);


--
-- Name: Description; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Description" (
    id uuid NOT NULL,
    label character varying(250) NOT NULL,
    plan uuid NOT NULL,
    properties text,
    description text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    finalized_at timestamp without time zone,
    description_template uuid NOT NULL,
    created_by uuid NOT NULL,
    plan_description_template uuid NOT NULL,
    tenant uuid,
    status uuid NOT NULL
);


--
-- Name: DescriptionReference; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."DescriptionReference" (
    id uuid NOT NULL,
    data text,
    description_id uuid NOT NULL,
    reference_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    tenant uuid
);


--
-- Name: DescriptionStatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."DescriptionStatus" (
    id uuid NOT NULL,
    name character varying(250) NOT NULL,
    description text,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    internal_status smallint,
    definition xml,
    tenant uuid,
    action character varying(250),
    ordinal integer NOT NULL
);


--
-- Name: DescriptionTag; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."DescriptionTag" (
    id uuid NOT NULL,
    description uuid NOT NULL,
    tag uuid NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid
);


--
-- Name: DescriptionTemplate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."DescriptionTemplate" (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    label character varying(250) NOT NULL,
    definition xml NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    description text NOT NULL,
    group_id uuid NOT NULL,
    version integer NOT NULL,
    language character varying NOT NULL,
    type uuid NOT NULL,
    is_active smallint NOT NULL,
    version_status smallint NOT NULL,
    tenant uuid,
    code character varying(200) NOT NULL
);


--
-- Name: DescriptionTemplateType; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."DescriptionTemplateType" (
    id uuid NOT NULL,
    name character varying(250) NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid,
    code character varying(200) NOT NULL
);


--
-- Name: DescriptionWorkflow; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."DescriptionWorkflow" (
    id uuid NOT NULL,
    name character varying(250) NOT NULL,
    description text,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    definition xml,
    tenant uuid
);


--
-- Name: EntityDoi; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."EntityDoi" (
    id uuid NOT NULL,
    repository_id character varying(150) NOT NULL,
    doi character varying(50) NOT NULL,
    created_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    updated_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    entity_id uuid NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    tenant uuid,
    entity_type smallint NOT NULL
);


--
-- Name: Evaluation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Evaluation" (
    id uuid NOT NULL,
    entity_type smallint NOT NULL,
    entity uuid NOT NULL,
    created_by uuid NOT NULL,
    evaluated_at timestamp without time zone NOT NULL,
    data character varying NOT NULL,
    status smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid
);


--
-- Name: Language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Language" (
    id uuid NOT NULL,
    code character varying(20) NOT NULL,
    payload text,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    ordinal integer NOT NULL,
    tenant uuid
);


--
-- Name: Lock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Lock" (
    id uuid NOT NULL,
    target uuid NOT NULL,
    target_type smallint NOT NULL,
    locked_by uuid NOT NULL,
    locked_at timestamp without time zone NOT NULL,
    touched_at timestamp without time zone,
    tenant uuid
);


--
-- Name: Plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Plan" (
    id uuid NOT NULL,
    label character varying(250) NOT NULL,
    version integer NOT NULL,
    properties text,
    group_id uuid NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    finalized_at timestamp without time zone,
    creator uuid NOT NULL,
    blueprint uuid NOT NULL,
    language character varying(100),
    access_type smallint,
    public_after timestamp without time zone,
    version_status smallint DEFAULT 1 NOT NULL,
    tenant uuid,
    status uuid NOT NULL
);


--
-- Name: PlanBlueprint; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PlanBlueprint" (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    label character varying(250) NOT NULL,
    definition xml,
    status smallint DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid,
    group_id uuid NOT NULL,
    version integer NOT NULL,
    version_status smallint NOT NULL,
    code character varying(200) NOT NULL,
    description text
);


--
-- Name: PlanDescriptionTemplate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PlanDescriptionTemplate" (
    id uuid NOT NULL,
    plan uuid NOT NULL,
    description_template_group uuid NOT NULL,
    section_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    tenant uuid
);


--
-- Name: PlanReference; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PlanReference" (
    id uuid NOT NULL,
    plan_id uuid NOT NULL,
    reference_id uuid NOT NULL,
    data text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    tenant uuid
);


--
-- Name: PlanStatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PlanStatus" (
    id uuid NOT NULL,
    name character varying(250) NOT NULL,
    description text,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    internal_status smallint,
    tenant uuid,
    definition xml,
    action character varying(250),
    ordinal integer NOT NULL
);


--
-- Name: PlanUser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PlanUser" (
    id uuid NOT NULL,
    plan uuid NOT NULL,
    "user" uuid NOT NULL,
    role smallint DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    tenant uuid,
    section_id uuid,
    ordinal integer NOT NULL
);


--
-- Name: PlanWorkflow; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PlanWorkflow" (
    id uuid NOT NULL,
    name character varying(250) NOT NULL,
    description text,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    definition xml,
    tenant uuid
);


--
-- Name: PrefillingSource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."PrefillingSource" (
    id uuid NOT NULL,
    label character varying(250) NOT NULL,
    definition xml NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid,
    code character varying(100) NOT NULL
);


--
-- Name: QueueInbox; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."QueueInbox" (
    id uuid NOT NULL,
    queue character varying(200) NOT NULL,
    exchange character varying(200) NOT NULL,
    route character varying(200) NOT NULL,
    application_id character varying(100) NOT NULL,
    message_id uuid NOT NULL,
    message json NOT NULL,
    retry_count integer,
    status smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid,
    is_active smallint NOT NULL
);


--
-- Name: QueueOutbox; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."QueueOutbox" (
    id uuid NOT NULL,
    exchange character varying(200) NOT NULL,
    route character varying(200) NOT NULL,
    message_id uuid NOT NULL,
    notify_status smallint NOT NULL,
    retry_count integer NOT NULL,
    published_at timestamp without time zone,
    confirmed_at timestamp without time zone,
    tenant uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    message text NOT NULL,
    is_active smallint NOT NULL
);


--
-- Name: Reference; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Reference" (
    id uuid NOT NULL,
    label character varying(1024) NOT NULL,
    description text,
    abbreviation character varying(50),
    source character varying(1024),
    is_active smallint NOT NULL,
    created_by uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    type uuid NOT NULL,
    definition character varying,
    reference character varying(1024) NOT NULL,
    source_type smallint NOT NULL,
    tenant uuid
);


--
-- Name: ReferenceType; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ReferenceType" (
    id uuid NOT NULL,
    name character varying(250) NOT NULL,
    code character varying(100) NOT NULL,
    definition character varying NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid
);


--
-- Name: StorageFile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."StorageFile" (
    id uuid NOT NULL,
    file_ref character varying(100) NOT NULL,
    name character varying(250) NOT NULL,
    extension character varying(10) NOT NULL,
    mime_type character varying(200) NOT NULL,
    storage_type smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    purge_at timestamp without time zone,
    purged_at timestamp without time zone,
    owner uuid,
    tenant uuid
);


--
-- Name: SupportiveMaterial; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."SupportiveMaterial" (
    id uuid NOT NULL,
    type smallint NOT NULL,
    language_code character varying(20) NOT NULL,
    payload character varying NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid
);


--
-- Name: Tag; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Tag" (
    id uuid NOT NULL,
    label character varying(1024) NOT NULL,
    created_by uuid,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid
);


--
-- Name: Tenant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Tenant" (
    id uuid NOT NULL,
    name character varying(200) NOT NULL,
    code character varying(500) NOT NULL,
    description character varying NOT NULL,
    config text,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


--
-- Name: TenantConfiguration; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."TenantConfiguration" (
    id uuid NOT NULL,
    value character varying NOT NULL,
    type smallint NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid
);


--
-- Name: TenantUser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."TenantUser" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    tenant uuid NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


--
-- Name: UsageLimit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UsageLimit" (
    id uuid NOT NULL,
    label character varying NOT NULL,
    target_metric character varying NOT NULL,
    value integer NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid,
    definition xml NOT NULL
);


--
-- Name: User; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."User" (
    id uuid NOT NULL,
    name character varying(250),
    additional_info character varying,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL
);


--
-- Name: UserContactInfo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UserContactInfo" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    ordinal integer DEFAULT 0 NOT NULL,
    type smallint NOT NULL,
    value character varying(512) NOT NULL,
    created_at timestamp without time zone NOT NULL
);


--
-- Name: UserCredential; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UserCredential" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    data character varying,
    external_id character varying(512) NOT NULL,
    created_at timestamp without time zone NOT NULL
);


--
-- Name: UserDescriptionTemplate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UserDescriptionTemplate" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    description_template uuid NOT NULL,
    role smallint NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid
);


--
-- Name: UserRole; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UserRole" (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    "user" uuid NOT NULL,
    role character varying(512) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    tenant uuid
);


--
-- Name: UserSettings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."UserSettings" (
    id uuid NOT NULL,
    key character varying(500) NOT NULL,
    entity_id uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    type character varying(200) NOT NULL,
    value text NOT NULL,
    name character varying(500) NOT NULL,
    tenant uuid
);


--
-- Name: ant_Annotation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_Annotation" (
    id uuid NOT NULL,
    entity_id uuid NOT NULL,
    entity_type character varying(512) NOT NULL,
    anchor character varying(512),
    payload text,
    tenant uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    subject_id uuid NOT NULL,
    thread_id uuid,
    parent_id uuid,
    protection_type smallint NOT NULL,
    time_stamp timestamp without time zone NOT NULL
);


--
-- Name: ant_AnnotationStatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_AnnotationStatus" (
    id uuid NOT NULL,
    annotation_id uuid NOT NULL,
    status_id uuid NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid
);


--
-- Name: ant_EntityUser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_EntityUser" (
    id uuid NOT NULL,
    entity_id uuid NOT NULL,
    user_id uuid NOT NULL,
    tenant uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL
);


--
-- Name: ant_QueueInbox; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_QueueInbox" (
    id uuid NOT NULL,
    queue character varying(200) NOT NULL,
    exchange character varying(200) NOT NULL,
    route character varying(200) NOT NULL,
    application_id character varying(100) NOT NULL,
    message_id uuid NOT NULL,
    message json NOT NULL,
    retry_count integer,
    status smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid,
    is_active smallint NOT NULL
);


--
-- Name: ant_QueueOutbox; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_QueueOutbox" (
    id uuid NOT NULL,
    exchange character varying(200) NOT NULL,
    route character varying(200) NOT NULL,
    message_id uuid NOT NULL,
    notify_status smallint NOT NULL,
    retry_count integer NOT NULL,
    published_at timestamp without time zone,
    confirmed_at timestamp without time zone,
    tenant uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    message text NOT NULL,
    is_active smallint NOT NULL
);


--
-- Name: ant_Status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_Status" (
    id uuid NOT NULL,
    label character varying(200) NOT NULL,
    internal_status smallint,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid
);


--
-- Name: ant_Tenant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_Tenant" (
    id uuid NOT NULL,
    code character varying(200) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL
);


--
-- Name: ant_TenantUser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_TenantUser" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    tenant uuid NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


--
-- Name: ant_User; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_User" (
    id uuid NOT NULL,
    name character varying(250),
    additional_info character varying,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL
);


--
-- Name: ant_UserCredential; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ant_UserCredential" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    external_id character varying(512) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    data character varying
);


--
-- Name: ntf_InAppNotification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_InAppNotification" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    type uuid NOT NULL,
    read_time timestamp without time zone,
    tracking_state smallint NOT NULL,
    priority smallint NOT NULL,
    subject character varying,
    body character varying,
    extra_data character varying,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid,
    is_active smallint NOT NULL
);


--
-- Name: ntf_Notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_Notification" (
    id uuid NOT NULL,
    "user" uuid,
    tenant uuid,
    type uuid,
    contact_hint character varying(200),
    notified_at timestamp without time zone,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    data text,
    retry_count integer,
    tracking_data text,
    provenance_ref character varying(200),
    tracking_state smallint NOT NULL,
    tracking_process smallint NOT NULL,
    is_active smallint NOT NULL,
    contact_type_hint smallint,
    notify_state smallint,
    notified_with smallint
);


--
-- Name: ntf_NotificationTemplate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_NotificationTemplate" (
    id uuid NOT NULL,
    channel smallint NOT NULL,
    notification_type uuid NOT NULL,
    kind smallint NOT NULL,
    language_code character varying(200) NOT NULL,
    value text NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid
);


--
-- Name: ntf_QueueInbox; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_QueueInbox" (
    id uuid NOT NULL,
    queue character varying(200) NOT NULL,
    exchange character varying(200) NOT NULL,
    route character varying(200) NOT NULL,
    application_id character varying(100) NOT NULL,
    message_id uuid NOT NULL,
    message json NOT NULL,
    retry_count integer,
    status smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    tenant uuid,
    is_active smallint NOT NULL
);


--
-- Name: ntf_QueueOutbox; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_QueueOutbox" (
    id uuid NOT NULL,
    exchange character varying(200) NOT NULL,
    route character varying(200) NOT NULL,
    message_id uuid NOT NULL,
    notify_status smallint NOT NULL,
    retry_count integer NOT NULL,
    published_at timestamp without time zone,
    confirmed_at timestamp without time zone,
    tenant uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    message text NOT NULL,
    is_active smallint NOT NULL
);


--
-- Name: ntf_Tenant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_Tenant" (
    id uuid NOT NULL,
    code character varying(200) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL
);


--
-- Name: ntf_TenantConfiguration; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_TenantConfiguration" (
    id uuid NOT NULL,
    tenant uuid,
    type smallint NOT NULL,
    value character varying NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


--
-- Name: ntf_TenantUser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_TenantUser" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    tenant uuid NOT NULL,
    is_active smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


--
-- Name: ntf_User; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_User" (
    id uuid NOT NULL,
    name character varying(250),
    additional_info character varying,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL
);


--
-- Name: ntf_UserContactInfo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_UserContactInfo" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    ordinal integer DEFAULT 0 NOT NULL,
    type smallint NOT NULL,
    value character varying(512) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL
);


--
-- Name: ntf_UserCredential; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_UserCredential" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    external_id character varying(512) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    data character varying
);


--
-- Name: ntf_UserNotificationPreference; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."ntf_UserNotificationPreference" (
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    type uuid NOT NULL,
    channel smallint NOT NULL,
    ordinal numeric NOT NULL,
    tenant uuid,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL
);


--
-- Data for Name: ActionConfirmation; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: DBVersion; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.001', '2024-08-21 10:00:00+00', '2026-09-30 13:16:53.665304+00', 'Add action, ordinal columns on plan, description status table.');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.002', '2024-09-18 10:00:00+00', '2026-09-30 13:16:54.16915+00', 'Insert values for Plan, Description Status and Workflow.');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.003', '2024-09-19 10:00:00+00', '2026-09-30 13:16:54.700588+00', 'Migrate status collumns.');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.004', '2024-09-19 10:00:00+00', '2026-09-30 13:16:55.170701+00', 'Make kpi_user subject id nullable.');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.005', '2025-01-20 10:00:00+00', '2026-09-30 13:16:55.530397+00', 'Add evaluation table.');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.006', '2025-01-20 10:00:00+00', '2026-09-30 13:16:56.024462+00', 'Add description collumn to Plan Blueprint.');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.007', '2025-04-14 10:00:00+00', '2026-09-30 13:16:56.53172+00', 'Add reference types rows');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.008', '2025-05-13 10:00:00+00', '2026-09-30 13:16:57.029368+00', 'Update TenantUser and TenantConfiguration for inactive tenants');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.009', '2025-05-28 10:00:00+00', '2026-09-30 13:16:57.415822+00', 'Update datasets, publications, organizations and grants reference types');
INSERT INTO public."DBVersion" (key, version, "releasedAt", "deployedAt", description) VALUES ('DMPDB', '01.00.010', '2025-06-20 10:00:00+00', '2026-09-30 13:16:57.857696+00', 'Add ordinal collumn to  Plan User.');


--
-- Data for Name: Description; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: DescriptionReference; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: DescriptionStatus; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."DescriptionStatus" (id, name, description, created_at, updated_at, is_active, internal_status, definition, tenant, action, ordinal) VALUES ('60f5e529-7ed3-4be1-8754-ac8c7443f246', 'Canceled', NULL, '2024-09-17 15:37:42.256424', '2024-10-21 13:57:32.976705', 1, 2, '<?xml version="1.0" standalone="yes"?><definition><authorization><edit><roles><roles>Admin</roles></roles><plan_roles><plan_roles>Owner</plan_roles></plan_roles><allowAuthenticated>false</allowAuthenticated><allowAnonymous>false</allowAnonymous></edit></authorization><availableActions><action>Export</action></availableActions></definition>', NULL, 'Cancel', 0);
INSERT INTO public."DescriptionStatus" (id, name, description, created_at, updated_at, is_active, internal_status, definition, tenant, action, ordinal) VALUES ('978e6ff6-b5e9-4cee-86cb-bc7401ec4059', 'Draft', NULL, '2024-09-16 12:46:20.459486', '2024-10-21 13:57:47.351152', 1, 0, '<?xml version="1.0" standalone="yes"?><definition><authorization><edit><roles><roles>Admin</roles></roles><plan_roles><plan_roles>Owner</plan_roles><plan_roles>Viewer</plan_roles><plan_roles>DescriptionContributor</plan_roles><plan_roles>Reviewer</plan_roles></plan_roles><allowAuthenticated>false</allowAuthenticated><allowAnonymous>false</allowAnonymous></edit></authorization><availableActions><action>Export</action></availableActions></definition>', NULL, NULL, 0);
INSERT INTO public."DescriptionStatus" (id, name, description, created_at, updated_at, is_active, internal_status, definition, tenant, action, ordinal) VALUES ('c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa', 'Finalized', NULL, '2024-09-17 07:38:15.846399', '2024-10-22 09:06:01.501559', 1, 1, '<?xml version="1.0" standalone="yes"?><definition><authorization><edit><roles><roles>Admin</roles></roles><plan_roles><plan_roles>Owner</plan_roles><plan_roles>DescriptionContributor</plan_roles></plan_roles><allowAuthenticated>false</allowAuthenticated><allowAnonymous>false</allowAnonymous></edit></authorization><availableActions><action>Export</action></availableActions></definition>', NULL, 'Finalize', 0);


--
-- Data for Name: DescriptionTag; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: DescriptionTemplate; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: DescriptionTemplateType; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."DescriptionTemplateType" (id, name, status, created_at, updated_at, is_active, tenant, code) VALUES ('709a8400-10ca-11ee-be56-0242ac120002', 'Dataset', 1, '2024-09-06 14:42:43.525287', '2024-09-06 14:42:43.525287', 1, NULL, '6166bcf6-6650-48d3-8cf8-73b5d623ecf1');
INSERT INTO public."DescriptionTemplateType" (id, name, status, created_at, updated_at, is_active, tenant, code) VALUES ('3b15c046-a978-4b5a-9376-66525b7f1ac9', 'Software', 1, '2024-09-06 14:42:43.525287', '2024-09-06 14:42:43.525287', 1, NULL, 'd8ca6985-593d-4ff9-a1b9-9e4a6fd51b75');


--
-- Data for Name: DescriptionWorkflow; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."DescriptionWorkflow" (id, name, description, created_at, updated_at, is_active, definition, tenant) VALUES ('8651af83-8b24-4776-ae45-329031db9f5e', 'default', NULL, '2024-09-17 07:39:00.221933', '2024-09-18 10:51:14.478419', 1, '<?xml version="1.0" standalone="yes"?><definition><startingStatusId>978e6ff6-b5e9-4cee-86cb-bc7401ec4059</startingStatusId><statusTransitions><fromStatusId>978e6ff6-b5e9-4cee-86cb-bc7401ec4059</fromStatusId><toStatusId>c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa</toStatusId></statusTransitions><statusTransitions><fromStatusId>c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa</fromStatusId><toStatusId>978e6ff6-b5e9-4cee-86cb-bc7401ec4059</toStatusId></statusTransitions></definition>', NULL);


--
-- Data for Name: EntityDoi; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: Evaluation; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: Language; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('94b2ead6-e27c-45f5-a986-14dda5a58376', 'en', NULL, '2023-11-28 16:35:18.114652', '2023-11-29 15:50:57.253892', 1, 0, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('d8bb2f19-048d-4806-b99a-efdafe36de52', 'de', NULL, '2023-11-28 16:35:18.114652', '2023-11-29 15:50:57.253892', 1, 1, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('db731f55-dc50-43c5-9fa3-680c84a6f735', 'fr', NULL, '2026-04-30 16:35:18.114652', '2026-04-30 16:35:18.114652', 1, 2, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('fe43112c-7e44-403f-bbc3-ac874dd62772', 'it', NULL, '2026-04-30 16:35:18.114652', '2026-04-30 16:35:18.114652', 1, 3, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('1213a820-e6bb-492e-b63b-06a8c650ef92', 'es', NULL, '2023-11-28 16:35:18.114652', '2023-11-29 15:50:57.253892', 1, 4, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('1f26b803-df4b-4270-82b1-895bfff9ccf2', 'baq', NULL, '2024-02-29 16:35:18.114652', '2024-02-29 16:35:18.114652', 1, 5, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('acb16ec4-dbad-4aec-b6a0-214ebacc2983', 'hr', NULL, '2023-11-28 16:35:18.114652', '2023-11-29 15:50:57.253892', 1, 6, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('bbdbd1fd-eb04-43a5-a359-150920f4ef76', 'cs', NULL, '2026-04-30 16:35:18.114652', '2026-04-30 16:35:18.114652', 1, 7, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('b8e6b183-7001-4598-91be-cce30e678754', 'nl', NULL, '2026-04-30 16:35:18.114652', '2026-04-30 16:35:18.114652', 1, 8, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('00e81ec3-9cd4-4126-ae5f-cbf4e92146e7', 'fi', NULL, '2026-04-30 16:35:18.114652', '2026-04-30 16:35:18.114652', 1, 9, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('defd282e-48d0-4f69-9b99-4137d5ce05af', 'gr', NULL, '2023-11-28 16:35:18.114652', '2023-11-29 15:50:57.253892', 1, 10, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('3b865f61-a863-43de-98cd-da86fc3f5940', 'pl', NULL, '2024-02-29 16:35:18.114652', '2024-02-29 16:35:18.114652', 1, 11, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('110d2e49-1580-4ee3-b1df-84da966f97a8', 'pt', NULL, '2024-02-29 16:35:18.114652', '2024-02-29 16:35:18.114652', 1, 12, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('96028d1e-99ce-49c6-851a-af9068d93891', 'ro', NULL, '2026-04-30 16:35:18.114652', '2026-04-30 16:35:18.114652', 1, 13, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('57c6129a-a918-4fb0-9c5b-9ec1c2ee1341', 'sr', NULL, '2024-02-29 16:35:18.114652', '2024-02-29 16:35:18.114652', 1, 14, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('cdbb160f-a655-46eb-8328-589144e5a484', 'sk', NULL, '2024-02-29 16:35:18.114652', '2024-02-29 16:35:18.114652', 1, 15, NULL);
INSERT INTO public."Language" (id, code, payload, created_at, updated_at, is_active, ordinal, tenant) VALUES ('96b9c91a-a9e4-4a66-9361-a1eba22cc971', 'tr', NULL, '2024-02-29 16:35:18.114652', '2024-02-29 16:35:18.114652', 1, 16, NULL);


--
-- Data for Name: Lock; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: Plan; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: PlanBlueprint; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."PlanBlueprint" (id, label, definition, status, created_at, updated_at, is_active, tenant, group_id, version, version_status, code, description) VALUES ('86635178-36a6-484f-9057-a934e4eeecd5', 'Dmp Default Blueprint', '<?xml version="1.0" standalone="yes"?><root><sections><section id="f94e50e0-cb97-4c65-8b88-e5db6badd41d" label="Main Info" description="" ordinal="1" hasTemplates="false" prefillingSourcesEnabled="false" canEditDescriptionTemplates="true"><fields><system type="Title" id="e62c3fa2-4cbe-41bf-a00e-ad722c7c7da1" category="System" label="Title of DMP" placeholder="Title of DMP" description="" ordinal="1" required="true"/><system type="Description" id="eed2871d-5201-401e-8453-87afbeac77e3" category="System" label="Description" placeholder="Fill with description" description="Briefly describe the context and purpose of the DMP" ordinal="2" required="true"/><referenceType referenceTypeId="5a2112e7-ea99-4cfe-98a1-68665e26726e" multipleSelect="true" id="41a0b1db-b186-467d-8edf-7d3b2456f95e" category="ReferenceType" label="Researchers" placeholder="Select researchers" description="Add here the names of people that have produced, processed, analysed the data described in the DMP." ordinal="3" required="false"/><referenceType referenceTypeId="7eeffb98-58fb-4921-82ec-e27f32f8e738" multipleSelect="true" id="0f0afb55-a11b-41db-a4d0-67ebb74d8685" category="ReferenceType" label="Organizations" placeholder="Select organization" description="Add here the names of the organizations contributing to the creation and revision of the DMPs" ordinal="4" required="false"/><system type="Language" id="db493026-3130-4730-9b93-da6b77d2eea4" category="System" label="Language" placeholder="Language" description="Select the language of your DMP" ordinal="5" required="true"/><system type="Contact" id="22deb104-1c46-4ea8-8261-8fdc9500dca2" category="System" label="Contact" placeholder="Contact" description="" ordinal="6" required="true"/></fields></section><section id="3c2608e5-9320-4d94-9ed7-1eab9500d84b" label="Funding" description="" ordinal="2" hasTemplates="false" prefillingSourcesEnabled="false" canEditDescriptionTemplates="true"><fields><referenceType referenceTypeId="538928bb-c7c6-452e-b66d-08e539f5f082" multipleSelect="false" id="527af8fe-27b0-4715-9e1e-35a2fce834a1" category="ReferenceType" label="Funding organizations" placeholder="Funder" description="Select a funder of your research or add new" ordinal="1" required="true"/><referenceType referenceTypeId="5b9c284f-f041-4995-96cc-fad7ad13289c" multipleSelect="false" id="4d12904e-2501-4d57-8d2b-1ac795c297e7" category="ReferenceType" label="Grants" placeholder="Grant" description="Find the grant of your research or add new" ordinal="2" required="true"/><referenceType referenceTypeId="3d372db5-a456-45e6-a845-e41e1a8311f8" multipleSelect="false" id="82f235c8-98fc-48d1-8245-36ab08f01036" category="ReferenceType" label="Project" placeholder="Project" description="Projects in OpenCDMP are perceived as distinct activities falling under a grant or common activities under different grants in collaborative schemas, eg open call for contributions. Please complete it for the grant associated to your organization if your project falls under this category. In all other cases, please leave blank and it will be autocompleted." ordinal="3" required="false"/></fields></section><section id="2a77e1f6-9989-4aeb-acd9-48e911a92abd" label="License" description="" ordinal="3" hasTemplates="false" prefillingSourcesEnabled="false" canEditDescriptionTemplates="true"><fields><referenceType referenceTypeId="2baab1e8-561f-4c15-84c3-571b811c52f6" multipleSelect="true" id="75c31705-6731-45dd-9853-75c5d0627439" category="ReferenceType" label="License" placeholder="License" description="Assign a license to your DMP by selecting the most appropriate from the list." ordinal="1" required="false"/><system type="AccessRights" id="990df932-eae6-44ca-85da-5378b010f439" category="System" label="Access Rights" placeholder="Access Rights" description="Choose how the DMP is displayed after is published on Zenodo. By choosing Open Access, the DMP will be open on Zenodo after the Publication Date. By choosing Restricted Access, the DMP will be restricted after the publication is made." ordinal="2" required="true"/></fields></section><section id="0db7845b-0e7c-41df-8d91-cbca97995fd5" label="Templates" description="" ordinal="4" hasTemplates="true" prefillingSourcesEnabled="false" canEditDescriptionTemplates="true"/></sections></root>', 1, '2024-02-15 10:26:49.804431', '2024-02-15 10:26:49.804431', 1, NULL, '325fc092-e521-4c4a-86da-2ebda77fe323', 1, 0, '325fc092-e521-4c4a-86da-2ebda77fe323', NULL);


--
-- Data for Name: PlanDescriptionTemplate; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: PlanReference; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: PlanStatus; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."PlanStatus" (id, name, description, created_at, updated_at, is_active, internal_status, tenant, definition, action, ordinal) VALUES ('313cce74-f44b-4a72-9cd8-a9c75fe03a7e', 'Under Review', NULL, '2024-09-17 07:36:58.677058', '2024-10-21 13:58:23.975388', 1, NULL, NULL, '<?xml version="1.0" standalone="yes"?><definition><authorization><edit><roles><roles>Admin</roles></roles><plan_roles><plan_roles>Owner</plan_roles></plan_roles><allowAuthenticated>false</allowAuthenticated><allowAnonymous>false</allowAnonymous></edit></authorization><availableActions><action>Export</action></availableActions></definition>', 'Review', 0);
INSERT INTO public."PlanStatus" (id, name, description, created_at, updated_at, is_active, internal_status, tenant, definition, action, ordinal) VALUES ('61fd91f5-c63a-45bc-aa7a-e1f00fbd8545', 'Validated', NULL, '2024-09-17 07:36:38.386887', '2024-10-21 13:58:34.360884', 1, NULL, NULL, '<?xml version="1.0" standalone="yes"?><definition><authorization><edit><roles><roles>Admin</roles></roles><plan_roles><plan_roles>Owner</plan_roles><plan_roles>Reviewer</plan_roles></plan_roles><allowAuthenticated>false</allowAuthenticated><allowAnonymous>false</allowAnonymous></edit></authorization><availableActions><action>Export</action></availableActions></definition>', 'Validate', 0);
INSERT INTO public."PlanStatus" (id, name, description, created_at, updated_at, is_active, internal_status, tenant, definition, action, ordinal) VALUES ('cb3ced76-9807-4829-82da-75777de1bc78', 'Draft', NULL, '2024-09-17 07:37:24.288404', '2024-10-22 09:05:11.141862', 1, 0, NULL, '<?xml version="1.0" standalone="yes"?><definition><authorization><edit><roles><roles>Admin</roles></roles><plan_roles><plan_roles>Owner</plan_roles><plan_roles>Viewer</plan_roles><plan_roles>DescriptionContributor</plan_roles><plan_roles>Reviewer</plan_roles></plan_roles><allowAuthenticated>false</allowAuthenticated><allowAnonymous>false</allowAnonymous></edit></authorization><availableActions><action>Export</action></availableActions></definition>', NULL, 0);
INSERT INTO public."PlanStatus" (id, name, description, created_at, updated_at, is_active, internal_status, tenant, definition, action, ordinal) VALUES ('f1a3da63-0bff-438f-8b46-1a81ca176115', 'Finalized', NULL, '2024-09-16 14:16:56.685177', '2024-10-22 09:05:29.762308', 1, 1, NULL, '<?xml version="1.0" standalone="yes"?><definition><authorization><edit><roles><roles>Admin</roles></roles><plan_roles><plan_roles>Owner</plan_roles></plan_roles><allowAuthenticated>false</allowAuthenticated><allowAnonymous>false</allowAnonymous></edit></authorization><availableActions><action>Deposit</action><action>Export</action></availableActions></definition>', 'Finalize', 0);


--
-- Data for Name: PlanUser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: PlanWorkflow; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."PlanWorkflow" (id, name, description, created_at, updated_at, is_active, definition, tenant) VALUES ('44df0e24-7879-48cc-bbe0-cd8a2b618855', 'default', NULL, '2024-09-18 11:39:30.5974', '2024-09-18 11:39:30.611945', 1, '<?xml version="1.0" standalone="yes"?><definition><startingStatusId>cb3ced76-9807-4829-82da-75777de1bc78</startingStatusId><statusTransitions><fromStatusId>cb3ced76-9807-4829-82da-75777de1bc78</fromStatusId><toStatusId>313cce74-f44b-4a72-9cd8-a9c75fe03a7e</toStatusId></statusTransitions><statusTransitions><fromStatusId>313cce74-f44b-4a72-9cd8-a9c75fe03a7e</fromStatusId><toStatusId>61fd91f5-c63a-45bc-aa7a-e1f00fbd8545</toStatusId></statusTransitions><statusTransitions><fromStatusId>61fd91f5-c63a-45bc-aa7a-e1f00fbd8545</fromStatusId><toStatusId>f1a3da63-0bff-438f-8b46-1a81ca176115</toStatusId></statusTransitions><statusTransitions><fromStatusId>f1a3da63-0bff-438f-8b46-1a81ca176115</fromStatusId><toStatusId>cfa5cff0-0cbf-4ae7-87b7-263343991f35</toStatusId></statusTransitions><statusTransitions><fromStatusId>cb3ced76-9807-4829-82da-75777de1bc78</fromStatusId><toStatusId>f1a3da63-0bff-438f-8b46-1a81ca176115</toStatusId></statusTransitions><statusTransitions><fromStatusId>f1a3da63-0bff-438f-8b46-1a81ca176115</fromStatusId><toStatusId>cb3ced76-9807-4829-82da-75777de1bc78</toStatusId></statusTransitions></definition>', NULL);


--
-- Data for Name: PrefillingSource; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: QueueInbox; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: QueueOutbox; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: Reference; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ReferenceType; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('8d68bc92-b83c-429a-841a-de202f56d3a2', 'CESSDA Data Catalogue  ', 'cessda-dc', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="studyNumber" label="studyNumber" dataType="Text"/><field code="studyUrl" label="studyUrl" dataType="Text"/><field code="titleStudy" label="titleStudy" dataType="Text"/></fields><sources><api><key>cessda-dc</key><label>cessda-dc</label><ordinal>1</ordinal><type>0</type><authentication><enabled>false</enabled></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[*]</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue>*</defaultValue><name>like</name></query><query><cases><case><value>{pageSize}</value></case></cases><defaultValue>100</defaultValue><name>pageSize</name></query><query><cases><case><value>{page}</value></case></cases><defaultValue>0</defaultValue><name>page</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>titleStudy</responsePath></field><field><code>description</code><responsePath>abstract</responsePath></field><field><code>studyNumber</code><responsePath>studyNumber</responsePath></field><field><code>studyUrl</code><responsePath>studyUrl</responsePath></field><field><code>titleStudy</code><responsePath>titleStudy</responsePath></field></fieldsMapping><resultsArrayPath>$[''Results''][*]</resultsArrayPath></results><url>https://datacatalogue.cessda.eu/api/DataSets/v2/search?q={like}&amp;limit={pageSize}&amp;offset={page}&amp;metadataLanguage=en</url></api></sources></definition>', 1, '2024-12-12 10:36:34.55785', '2025-01-08 09:14:54.289168', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('a16b67be-9020-459f-982e-1edf7abf65fe', 'CESSDA Vocabulary Service', 'cessda-vs', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><api><key>cessda-vs</key><label>cessda-vs</label><ordinal>1</ordinal><type>0</type><authentication><enabled>false</enabled></authentication><contentType>application/Id+json</contentType><filterType>remote</filterType><requestHttpMethod>0</requestHttpMethod><paginationPath>$[*]</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue>*</defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>type</responsePath></field><field><code>label</code><responsePath>notation</responsePath></field><field><code>description</code><responsePath>definition</responsePath></field></fieldsMapping><resultsArrayPath>$["results"][*]</resultsArrayPath></results><url>https://vocabularies.cessda.eu/v2/search/codes?agency=CESSDA&amp;lang=en&amp;query={like}&amp;size=100&amp;vocab=TopicClassification</url></api></sources></definition>', 1, '2024-12-17 09:22:49.578739', '2024-12-20 12:07:47.830831', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('85996546-aa12-489d-9bb6-a8b5af7bb2a3', 'CESSDA ELSST ', 'cessda-elsst', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><api><key>cessda-elsst</key><label>cessda-elsst</label><ordinal>1</ordinal><type>0</type><authentication><enabled>false</enabled></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[*]</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue>en</defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>title</responsePath></field><field><code>description</code><responsePath>uri</responsePath></field></fieldsMapping><resultsArrayPath>$[''vocabularies''][*]</resultsArrayPath></results><url>https://thesauri.cessda.eu/rest/v1/vocabularies?lang={like}</url></api></sources></definition>', 1, '2024-12-17 11:29:41.81942', '2024-12-17 11:29:41.822417', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('1154a51e-2a2d-4f45-a42c-e3b586f79305', 'FAIRsharing Policies', 'fair-sharing-policies', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="internal-id" label="Internal Id" description="Internal FAIRsharing Id" dataType="Text"/><field code="abbreviation" label="Abbreviation" description="abbreviation" dataType="Text"/></fields><sources><api><key>FAIRsharing</key><label>FAIRsharing</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>1</authUrlMethod><authUrlBody>{    "user":       {           "login": "kvlachos-cite",           "password": "zF0EMQ4rPobx60Vyfvi4"       } }  </authUrlBody><authTokenPath>jwt</authTokenPath><authUrl>https://api.fairsharing.org/users/sign_in</authUrl><enabled>true</enabled><authType>Bearer</authType></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>1</requestHttpMethod><paginationPath>$[*]</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue>*</defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>["attributes"]["metadata"]doi</responsePath></field><field><code>label</code><responsePath>["attributes"]["metadata"]name</responsePath></field><field><code>description</code><responsePath>["attributes"]["metadata"]description</responsePath></field><field><code>internal-id</code><responsePath>id</responsePath></field><field><code>abbreviation</code><responsePath>["attributes"]["metadata"]abbreviation</responsePath></field></fieldsMapping><resultsArrayPath>$[''data''][*]</resultsArrayPath></results><url>https://api.fairsharing.org/search/fairsharing_records?q={like}&amp;fairsharing_registry=policy&amp;status=ready</url></api></sources></definition>', 1, '2025-04-03 12:24:36.376605', '2025-04-04 07:28:05.615994', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('eb36397b-d8c6-4223-a012-3a8a7ed813a2', 'FAIRsharing Standard', 'fair-sharing-standard', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="internal-id" label="Internal Id" description="Internal FAIRsharing Id" dataType="Text"/><field code="abbreviation" label="Abbreviation" description="abbreviation" dataType="Text"/></fields><sources><api><key>FAIRsharing</key><label>FAIRsharing</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>1</authUrlMethod><authUrlBody>{    "user":       {           "login": "kvlachos-cite",           "password": "zF0EMQ4rPobx60Vyfvi4"       } }  </authUrlBody><authTokenPath>jwt</authTokenPath><authUrl>https://api.fairsharing.org/users/sign_in</authUrl><enabled>true</enabled><authType>Bearer</authType></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>1</requestHttpMethod><paginationPath>$[*]</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue>*</defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>["attributes"]["metadata"]doi</responsePath></field><field><code>label</code><responsePath>["attributes"]["metadata"]name</responsePath></field><field><code>description</code><responsePath>["attributes"]["metadata"]description</responsePath></field><field><code>internal-id</code><responsePath>id</responsePath></field><field><code>abbreviation</code><responsePath>["attributes"]["metadata"]abbreviation</responsePath></field></fieldsMapping><resultsArrayPath>$[''data''][*]</resultsArrayPath></results><url>https://api.fairsharing.org/search/fairsharing_records?q={like}&amp;fairsharing_registry=standard&amp;status=ready</url></api></sources></definition>', 1, '2025-04-04 07:20:58.978498', '2025-04-04 07:30:20.461035', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('55caceff-b89e-4f34-969f-39d391c20702', 'Field of Science', 'fos', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="code" label="code" description="code" dataType="Text"/><field code="level" label="level" description="level" dataType="Text"/></fields><sources><api><key>openaire</key><label>OpenAIRE</label><ordinal>1</ordinal><type>0</type><authentication><enabled>false</enabled></authentication><contentType>application/json</contentType><filterType>remote</filterType><requestHttpMethod>0</requestHttpMethod><paginationPath>fos</paginationPath><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>label</responsePath></field><field><code>description</code><responsePath>label</responsePath></field><field><code>code</code><responsePath>code</responsePath></field><field><code>level</code><responsePath>level</responsePath></field></fieldsMapping><resultsArrayPath>fos</resultsArrayPath></results><url>https://explore.openaire.eu/assets/common-assets/vocabulary/fos.json</url></api></sources></definition>', 1, '2025-04-14 10:25:17.609064', '2025-04-14 10:32:11.798345', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('2baab1e8-561f-4c15-84c3-571b811c52f6', 'Licenses', 'licenses', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="uri" label="uri" description="uri" dataType="Text" required="false"><semantics><semantic>zenodo.metadata.rights.link</semantic></semantics></field></fields><sources><api><key>opendefinition</key><label>Open Definition</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/vnd.api+json; charset=utf-8</contentType><filterType>local</filterType><firstPage>1</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[*]</paginationPath><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>title</responsePath></field><field><code>description</code><responsePath>maintainer</responsePath></field><field><code>uri</code><responsePath>url</responsePath></field></fieldsMapping><resultsArrayPath>$[*]</resultsArrayPath></results><url>https://licenses.opendefinition.org/licenses/groups/all.json</url></api></sources></definition>', 1, '2023-11-16 16:24:12.162675', '2025-10-17 13:04:06.043208', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('2beacaad-3223-43ad-ad99-1e5f21328e7b', 'Registries', 'registries', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="url" label="url" description="url" dataType="Text"/></fields><sources><api><key>rda-metadata-schemes</key><label>RDA Metadata Schemes</label><ordinal>1</ordinal><referenceTypeDependencyIds/><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json; charset=utf-8</contentType><filterType>remote</filterType><firstPage>1</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>mscid</responsePath></field><field><code>label</code><responsePath>title</responsePath></field><field><code>description</code><responsePath>description</responsePath></field><field><code>url</code><responsePath>uri</responsePath></field></fieldsMapping><resultsArrayPath>$[''data''][''items''][*]</resultsArrayPath></results><url>https://rdamsc.bath.ac.uk/api2/m?q={like}&amp;start=1&amp;pageSize={pageSize}</url></api></sources></definition>', 1, '2023-11-16 10:08:21.240804', '2024-02-16 09:16:50.456347', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('3d372db5-a456-45e6-a845-e41e1a8311f8', 'Projects', 'projects', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><static><key>project</key><label>project</label><ordinal>1</ordinal><type>1</type><items><item><options><option><code>reference_id</code><value>cite_project</value></option><option><code>label</code><value>Cite Project</value></option><option><code>description</code><value>Cite Project</value></option></options></item></items></static></sources></definition>', 1, '2023-11-17 08:55:05.190807', '2023-11-17 08:56:23.012619', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('5a2112e7-ea99-4cfe-98a1-68665e26726e', 'Researchers', 'researchers', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="firstName" label="Researchers First Name" description="Researchers First Name" dataType="Text" required="false"><semantics><semantic>zenodo.metadata.contributors.person_or_org.given_name</semantic></semantics></field><field code="lastName" label="Researchers Last Name" description="Researchers Last Name" dataType="Text" required="false"><semantics><semantic>zenodo.metadata.contributors.person_or_org.family_name</semantic></semantics></field></fields><sources><api><key>orcid</key><label>ORCID</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json; charset=UTF-8</contentType><filterType>remote</filterType><firstPage>0</firstPage><headers><headers><key>ACCEPT</key><value>application/json; charset=UTF-8</value></headers></headers><requestHttpMethod>0</requestHttpMethod><paginationPath>$[''num-found'']</paginationPath><queries><query><cases><case><likePattern>^[A-Za-z0-9]+ [A-Za-z0-9]+$</likePattern><separator> </separator><value>((given-names:"{like1}"+AND+family-name:"{like2}")+OR+(given-names:"{like2}"+AND+family-name:"{like1}"))</value></case><case><likePattern>\d{4}-\d{4}-\d{4}-\d{4}</likePattern><value>orcid:"{like}"</value></case><case><likePattern>.+</likePattern><value>given-names:"{like}"+OR+family-name:"{like}"</value></case></cases><defaultValue>*</defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>orcid-id</responsePath></field><field><code>label</code><responsePath>@{{given-names}} @{{family-names}} (@{{orcid-id}})</responsePath></field><field><code>description</code><responsePath>institution-name</responsePath></field><field><code>firstName</code><responsePath>given-names</responsePath></field><field><code>lastName</code><responsePath>family-names</responsePath></field></fieldsMapping><resultsArrayPath>$[''expanded-result''][*]</resultsArrayPath></results><url>https://pub.orcid.org/v3.0/expanded-search/?q={like}&amp;start={page}&amp;rows={pageSize}</url></api></sources></definition>', 1, '2023-11-16 18:21:43.272982', '2025-10-17 12:08:53.831211', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('ab7cdd93-bea2-440d-880d-3846dad80b21', 'Taxonomies', 'taxonomies', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><static><key>taxonomy</key><label>Taxonomies</label><ordinal>1</ordinal><type>1</type><items><item><options><option><code>reference_id</code><value>cite_taxonomy</value></option><option><code>label</code><value>Cite Taxonom</value></option><option><code>description</code><value>Cite Taxonom</value></option></options></item></items></static></sources></definition>', 1, '2023-11-16 17:48:09.769599', '2024-04-25 12:36:57.923984', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('8ec7556b-749d-4c4a-a4b9-43d064693795', 'Journals', 'journals', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><api><key>openaire</key><label>OpenAIRE Journals</label><ordinal>1</ordinal><referenceTypeDependencyIds/><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>1</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><name>like</name></query></queries><requestBody>{"officialname": "{like}", "typology":"journal"}</requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>officialname</responsePath></field><field><code>description</code><responsePath>officialname</responsePath></field></fieldsMapping><resultsArrayPath>$[''datasourceInfo''][*]</resultsArrayPath></results><url>https://services.openaire.eu/openaire/ds/searchdetails/{page}/{pageSize}?requestSortBy=id&amp;order=ASCENDING</url></api></sources></definition>', 1, '2023-11-16 17:40:12.811667', '2024-02-16 09:09:22.816978', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('9ec2000d-95c7-452e-b356-755fc8e2574c', 'Services', 'services', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="uri" label="Service Uri" description="Service Uri" dataType="Text"/></fields><sources><api><key>eosc</key><label>EOSC Providers</label><ordinal>1</ordinal><referenceTypeDependencyIds/><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json; charset=utf-8</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[''from'',''to'',''total'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>name</responsePath></field><field><code>description</code><responsePath>description</responsePath></field><field><code>uri</code><responsePath>webpage</responsePath></field></fieldsMapping><resultsArrayPath>$[''results''][*]</resultsArrayPath></results><url>https://providers.eosc-portal.eu/api/service/all?query={like}&amp;from={page}&amp;quantity={pageSize}</url></api></sources></definition>', 1, '2023-11-16 17:57:22.081053', '2024-02-16 09:07:13.944104', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('1e927daa-b856-443f-96da-22f325f7322f', 'Publication Repositories', 'pubRepositories', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><api><key>openaire</key><label>OpenAIRE publication repositories 1</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>1</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><name>like</name></query></queries><requestBody>{"officialname": "{like}", "typology":"pubsrepository::institutional"}</requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>officialname</responsePath></field><field><code>description</code><responsePath>officialname</responsePath></field></fieldsMapping><resultsArrayPath>$[''datasourceInfo''][*]</resultsArrayPath></results><url>https://services.openaire.eu/openaire/ds/searchdetails/{page}/{pageSize}?requestSortBy=id&amp;order=ASCENDING</url></api><api><key>openaire</key><label>OpenAIRE publication repositories 2</label><ordinal>2</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>1</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><name>like</name></query></queries><requestBody>{"officialname": "{like}", "typology":"pubsrepository::thematic"}</requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>officialname</responsePath></field><field><code>description</code><responsePath>officialname</responsePath></field></fieldsMapping><resultsArrayPath>$[''datasourceInfo''][*]</resultsArrayPath></results><url>https://services.openaire.eu/openaire/ds/searchdetails/{page}/{pageSize}?requestSortBy=id&amp;order=ASCENDING</url></api><api><key>openaire</key><label>OpenAIRE publication repositories 3</label><ordinal>3</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>1</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><name>like</name></query></queries><requestBody>{"officialname": "{like}", "typology":"pubsrepository::unknown"}</requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>officialname</responsePath></field><field><code>description</code><responsePath>officialname</responsePath></field></fieldsMapping><resultsArrayPath>$[''datasourceInfo''][*]</resultsArrayPath></results><url>https://services.openaire.eu/openaire/ds/searchdetails/{page}/{pageSize}?requestSortBy=id&amp;order=ASCENDING</url></api></sources></definition>', 1, '2023-11-16 17:17:40.882679', '2024-05-01 11:52:50.297337', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('a3ce0fb2-d72c-48bb-b322-7401940cb802', 'Datasets', 'datasets', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="pid" label="Pid Datasets" description="Pid Datasets" dataType="Text"/><field code="pidTypeField" label="PidTypeField Datasets" description="PidTypeField Datasets" dataType="Text"/></fields><sources><api><key>openaire</key><label>OpenAIRE</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json; charset=utf-8</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue></defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>header.originalId.first()</responsePath></field><field><code>label</code><responsePath>result.maintitle</responsePath></field><field><code>description</code><responsePath>result.maintitle</responsePath></field><field><code>pid</code><responsePath>result.instance.first().pid.first().value</responsePath></field><field><code>pidTypeField</code><responsePath>result.instance.first().pid.first().typeCode</responsePath></field></fieldsMapping><resultsArrayPath>$[''results''][*]</resultsArrayPath></results><url>https://services.openaire.eu/search/v2/api/datasets/?q={like}&amp;page={page}&amp;size={pageSize}&amp;format=json</url></api></sources></definition>', 1, '2023-11-17 10:26:55.332111', '2025-05-27 09:01:00.107335', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('f39dbc8c-1e31-48cd-a0b1-0bf2921d87c4', 'Currencies', 'currencies', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><static><key>currencies</key><label>currencies</label><ordinal>1</ordinal><type>1</type><items><item><options><option><code>reference_id</code><value>AFN</value></option><option><code>label</code><value>Afghani</value></option><option><code>description</code><value>Afghani</value></option></options></item><item><options><option><code>reference_id</code><value>EUR</value></option><option><code>label</code><value>Euro</value></option><option><code>description</code><value>Euro</value></option></options></item><item><options><option><code>reference_id</code><value>ALL</value></option><option><code>label</code><value>Lek</value></option><option><code>description</code><value>Lek</value></option></options></item><item><options><option><code>reference_id</code><value>DZD</value></option><option><code>label</code><value>Algerian Dinar</value></option><option><code>description</code><value>Algerian Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>USD</value></option><option><code>label</code><value>US Dollar</value></option><option><code>description</code><value>US Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>AOA</value></option><option><code>label</code><value>Kwanza</value></option><option><code>description</code><value>Kwanza</value></option></options></item><item><options><option><code>reference_id</code><value>XCD</value></option><option><code>label</code><value>East Caribbean Dollar</value></option><option><code>description</code><value>East Caribbean Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>ARS</value></option><option><code>label</code><value>Argentine Peso</value></option><option><code>description</code><value>Argentine Peso</value></option></options></item><item><options><option><code>reference_id</code><value>AMD</value></option><option><code>label</code><value>Armenian Dram</value></option><option><code>description</code><value>Armenian Dram</value></option></options></item><item><options><option><code>reference_id</code><value>AWG</value></option><option><code>label</code><value>Aruban Florin</value></option><option><code>description</code><value>Aruban Florin</value></option></options></item><item><options><option><code>reference_id</code><value>AUD</value></option><option><code>label</code><value>Australian Dollar</value></option><option><code>description</code><value>Australian Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>AZN</value></option><option><code>label</code><value>Azerbaijan Manat</value></option><option><code>description</code><value>Azerbaijan Manat</value></option></options></item><item><options><option><code>reference_id</code><value>BSD</value></option><option><code>label</code><value>Bahamian Dollar</value></option><option><code>description</code><value>Bahamian Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>BHD</value></option><option><code>label</code><value>Bahraini Dinar</value></option><option><code>description</code><value>Bahraini Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>BDT</value></option><option><code>label</code><value>Taka</value></option><option><code>description</code><value>Taka</value></option></options></item><item><options><option><code>reference_id</code><value>BBD</value></option><option><code>label</code><value>Barbados Dollar</value></option><option><code>description</code><value>Barbados Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>BYN</value></option><option><code>label</code><value>Belarusian Ruble</value></option><option><code>description</code><value>Belarusian Ruble</value></option></options></item><item><options><option><code>reference_id</code><value>BZD</value></option><option><code>label</code><value>Belize Dollar</value></option><option><code>description</code><value>Belize Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>XOF</value></option><option><code>label</code><value>CFA Franc BCEAO</value></option><option><code>description</code><value>CFA Franc BCEAO</value></option></options></item><item><options><option><code>reference_id</code><value>BMD</value></option><option><code>label</code><value>Bermudian Dollar</value></option><option><code>description</code><value>Bermudian Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>INR</value></option><option><code>label</code><value>Indian Rupee</value></option><option><code>description</code><value>Indian Rupee</value></option></options></item><item><options><option><code>reference_id</code><value>BTN</value></option><option><code>label</code><value>Ngultrum</value></option><option><code>description</code><value>Ngultrum</value></option></options></item><item><options><option><code>reference_id</code><value>BOB</value></option><option><code>label</code><value>Boliviano</value></option><option><code>description</code><value>Boliviano</value></option></options></item><item><options><option><code>reference_id</code><value>BOV</value></option><option><code>label</code><value>Mvdol</value></option><option><code>description</code><value>Mvdol</value></option></options></item><item><options><option><code>reference_id</code><value>BAM</value></option><option><code>label</code><value>Convertible Mark</value></option><option><code>description</code><value>Convertible Mark</value></option></options></item><item><options><option><code>reference_id</code><value>BWP</value></option><option><code>label</code><value>Pula</value></option><option><code>description</code><value>Pula</value></option></options></item><item><options><option><code>reference_id</code><value>NOK</value></option><option><code>label</code><value>Norwegian Krone</value></option><option><code>description</code><value>Norwegian Krone</value></option></options></item><item><options><option><code>reference_id</code><value>BRL</value></option><option><code>label</code><value>Brazilian Real</value></option><option><code>description</code><value>Brazilian Real</value></option></options></item><item><options><option><code>reference_id</code><value>BND</value></option><option><code>label</code><value>Brunei Dollar</value></option><option><code>description</code><value>Brunei Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>BGN</value></option><option><code>label</code><value>Bulgarian Lev</value></option><option><code>description</code><value>Bulgarian Lev</value></option></options></item><item><options><option><code>reference_id</code><value>BIF</value></option><option><code>label</code><value>Burundi Franc</value></option><option><code>description</code><value>Burundi Franc</value></option></options></item><item><options><option><code>reference_id</code><value>CVE</value></option><option><code>label</code><value>Cabo Verde Escudo</value></option><option><code>description</code><value>Cabo Verde Escudo</value></option></options></item><item><options><option><code>reference_id</code><value>KHR</value></option><option><code>label</code><value>Riel</value></option><option><code>description</code><value>Riel</value></option></options></item><item><options><option><code>reference_id</code><value>XAF</value></option><option><code>label</code><value>CFA Franc BEAC</value></option><option><code>description</code><value>CFA Franc BEAC</value></option></options></item><item><options><option><code>reference_id</code><value>CAD</value></option><option><code>label</code><value>Canadian Dollar</value></option><option><code>description</code><value>Canadian Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>KYD</value></option><option><code>label</code><value>Cayman Islands Dollar</value></option><option><code>description</code><value>Cayman Islands Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>CLP</value></option><option><code>label</code><value>Chilean Peso</value></option><option><code>description</code><value>Chilean Peso</value></option></options></item><item><options><option><code>reference_id</code><value>CLF</value></option><option><code>label</code><value>Unidad de Fomento</value></option><option><code>description</code><value>Unidad de Fomento</value></option></options></item><item><options><option><code>reference_id</code><value>CNY</value></option><option><code>label</code><value>Yuan Renminbi</value></option><option><code>description</code><value>Yuan Renminbi</value></option></options></item><item><options><option><code>reference_id</code><value>COP</value></option><option><code>label</code><value>Colombian Peso</value></option><option><code>description</code><value>Colombian Peso</value></option></options></item><item><options><option><code>reference_id</code><value>COU</value></option><option><code>label</code><value>Unidad de Valor Real</value></option><option><code>description</code><value>Unidad de Valor Real</value></option></options></item><item><options><option><code>reference_id</code><value>KMF</value></option><option><code>label</code><value>Comorian Franc </value></option><option><code>description</code><value>Comorian Franc </value></option></options></item><item><options><option><code>reference_id</code><value>CDF</value></option><option><code>label</code><value>Congolese Franc</value></option><option><code>description</code><value>Congolese Franc</value></option></options></item><item><options><option><code>reference_id</code><value>NZD</value></option><option><code>label</code><value>New Zealand Dollar</value></option><option><code>description</code><value>New Zealand Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>CRC</value></option><option><code>label</code><value>Costa Rican Colon</value></option><option><code>description</code><value>Costa Rican Colon</value></option></options></item><item><options><option><code>reference_id</code><value>HRK</value></option><option><code>label</code><value>Kuna</value></option><option><code>description</code><value>Kuna</value></option></options></item><item><options><option><code>reference_id</code><value>CUP</value></option><option><code>label</code><value>Cuban Peso</value></option><option><code>description</code><value>Cuban Peso</value></option></options></item><item><options><option><code>reference_id</code><value>CUC</value></option><option><code>label</code><value>Peso Convertible</value></option><option><code>description</code><value>Peso Convertible</value></option></options></item><item><options><option><code>reference_id</code><value>ANG</value></option><option><code>label</code><value>Netherlands Antillean Guilder</value></option><option><code>description</code><value>Netherlands Antillean Guilder</value></option></options></item><item><options><option><code>reference_id</code><value>CZK</value></option><option><code>label</code><value>Czech Koruna</value></option><option><code>description</code><value>Czech Koruna</value></option></options></item><item><options><option><code>reference_id</code><value>DKK</value></option><option><code>label</code><value>Danish Krone</value></option><option><code>description</code><value>Danish Krone</value></option></options></item><item><options><option><code>reference_id</code><value>DJF</value></option><option><code>label</code><value>Djibouti Franc</value></option><option><code>description</code><value>Djibouti Franc</value></option></options></item><item><options><option><code>reference_id</code><value>DOP</value></option><option><code>label</code><value>Dominican Peso</value></option><option><code>description</code><value>Dominican Peso</value></option></options></item><item><options><option><code>reference_id</code><value>EGP</value></option><option><code>label</code><value>Egyptian Pound</value></option><option><code>description</code><value>Egyptian Pound</value></option></options></item><item><options><option><code>reference_id</code><value>SVC</value></option><option><code>label</code><value>El Salvador Colon</value></option><option><code>description</code><value>El Salvador Colon</value></option></options></item><item><options><option><code>reference_id</code><value>ERN</value></option><option><code>label</code><value>Nakfa</value></option><option><code>description</code><value>Nakfa</value></option></options></item><item><options><option><code>reference_id</code><value>SZL</value></option><option><code>label</code><value>Lilangeni</value></option><option><code>description</code><value>Lilangeni</value></option></options></item><item><options><option><code>reference_id</code><value>ETB</value></option><option><code>label</code><value>Ethiopian Birr</value></option><option><code>description</code><value>Ethiopian Birr</value></option></options></item><item><options><option><code>reference_id</code><value>FKP</value></option><option><code>label</code><value>Falkland Islands Pound</value></option><option><code>description</code><value>Falkland Islands Pound</value></option></options></item><item><options><option><code>reference_id</code><value>FJD</value></option><option><code>label</code><value>Fiji Dollar</value></option><option><code>description</code><value>Fiji Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>XPF</value></option><option><code>label</code><value>CFP Franc</value></option><option><code>description</code><value>CFP Franc</value></option></options></item><item><options><option><code>reference_id</code><value>GMD</value></option><option><code>label</code><value>Dalasi</value></option><option><code>description</code><value>Dalasi</value></option></options></item><item><options><option><code>reference_id</code><value>GEL</value></option><option><code>label</code><value>Lari</value></option><option><code>description</code><value>Lari</value></option></options></item><item><options><option><code>reference_id</code><value>GHS</value></option><option><code>label</code><value>Ghana Cedi</value></option><option><code>description</code><value>Ghana Cedi</value></option></options></item><item><options><option><code>reference_id</code><value>GIP</value></option><option><code>label</code><value>Gibraltar Pound</value></option><option><code>description</code><value>Gibraltar Pound</value></option></options></item><item><options><option><code>reference_id</code><value>GTQ</value></option><option><code>label</code><value>Quetzal</value></option><option><code>description</code><value>Quetzal</value></option></options></item><item><options><option><code>reference_id</code><value>GBP</value></option><option><code>label</code><value>Pound Sterling</value></option><option><code>description</code><value>Pound Sterling</value></option></options></item><item><options><option><code>reference_id</code><value>GNF</value></option><option><code>label</code><value>Guinean Franc</value></option><option><code>description</code><value>Guinean Franc</value></option></options></item><item><options><option><code>reference_id</code><value>GYD</value></option><option><code>label</code><value>Guyana Dollar</value></option><option><code>description</code><value>Guyana Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>HTG</value></option><option><code>label</code><value>Gourde</value></option><option><code>description</code><value>Gourde</value></option></options></item><item><options><option><code>reference_id</code><value>HNL</value></option><option><code>label</code><value>Lempira</value></option><option><code>description</code><value>Lempira</value></option></options></item><item><options><option><code>reference_id</code><value>HKD</value></option><option><code>label</code><value>Hong Kong Dollar</value></option><option><code>description</code><value>Hong Kong Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>HUF</value></option><option><code>label</code><value>Forint</value></option><option><code>description</code><value>Forint</value></option></options></item><item><options><option><code>reference_id</code><value>ISK</value></option><option><code>label</code><value>Iceland Krona</value></option><option><code>description</code><value>Iceland Krona</value></option></options></item><item><options><option><code>reference_id</code><value>IDR</value></option><option><code>label</code><value>Rupiah</value></option><option><code>description</code><value>Rupiah</value></option></options></item><item><options><option><code>reference_id</code><value>XDR</value></option><option><code>label</code><value>SDR (Special Drawing Right)</value></option><option><code>description</code><value>SDR (Special Drawing Right)</value></option></options></item><item><options><option><code>reference_id</code><value>IRR</value></option><option><code>label</code><value>Iranian Rial</value></option><option><code>description</code><value>Iranian Rial</value></option></options></item><item><options><option><code>reference_id</code><value>IQD</value></option><option><code>label</code><value>Iraqi Dinar</value></option><option><code>description</code><value>Iraqi Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>ILS</value></option><option><code>label</code><value>New Israeli Sheqel</value></option><option><code>description</code><value>New Israeli Sheqel</value></option></options></item><item><options><option><code>reference_id</code><value>JMD</value></option><option><code>label</code><value>Jamaican Dollar</value></option><option><code>description</code><value>Jamaican Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>JPY</value></option><option><code>label</code><value>Yen</value></option><option><code>description</code><value>Yen</value></option></options></item><item><options><option><code>reference_id</code><value>JOD</value></option><option><code>label</code><value>Jordanian Dinar</value></option><option><code>description</code><value>Jordanian Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>KZT</value></option><option><code>label</code><value>Tenge</value></option><option><code>description</code><value>Tenge</value></option></options></item><item><options><option><code>reference_id</code><value>KES</value></option><option><code>label</code><value>Kenyan Shilling</value></option><option><code>description</code><value>Kenyan Shilling</value></option></options></item><item><options><option><code>reference_id</code><value>KPW</value></option><option><code>label</code><value>North Korean Won</value></option><option><code>description</code><value>North Korean Won</value></option></options></item><item><options><option><code>reference_id</code><value>KRW</value></option><option><code>label</code><value>Won</value></option><option><code>description</code><value>Won</value></option></options></item><item><options><option><code>reference_id</code><value>KWD</value></option><option><code>label</code><value>Kuwaiti Dinar</value></option><option><code>description</code><value>Kuwaiti Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>KGS</value></option><option><code>label</code><value>Som</value></option><option><code>description</code><value>Som</value></option></options></item><item><options><option><code>reference_id</code><value>LAK</value></option><option><code>label</code><value>Lao Kip</value></option><option><code>description</code><value>Lao Kip</value></option></options></item><item><options><option><code>reference_id</code><value>LBP</value></option><option><code>label</code><value>Lebanese Pound</value></option><option><code>description</code><value>Lebanese Pound</value></option></options></item><item><options><option><code>reference_id</code><value>LSL</value></option><option><code>label</code><value>Loti</value></option><option><code>description</code><value>Loti</value></option></options></item><item><options><option><code>reference_id</code><value>ZAR</value></option><option><code>label</code><value>Rand</value></option><option><code>description</code><value>Rand</value></option></options></item><item><options><option><code>reference_id</code><value>LRD</value></option><option><code>label</code><value>Liberian Dollar</value></option><option><code>description</code><value>Liberian Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>LYD</value></option><option><code>label</code><value>Libyan Dinar</value></option><option><code>description</code><value>Libyan Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>CHF</value></option><option><code>label</code><value>Swiss Franc</value></option><option><code>description</code><value>Swiss Franc</value></option></options></item><item><options><option><code>reference_id</code><value>MOP</value></option><option><code>label</code><value>Pataca</value></option><option><code>description</code><value>Pataca</value></option></options></item><item><options><option><code>reference_id</code><value>MKD</value></option><option><code>label</code><value>Denar</value></option><option><code>description</code><value>Denar</value></option></options></item><item><options><option><code>reference_id</code><value>MGA</value></option><option><code>label</code><value>Malagasy Ariary</value></option><option><code>description</code><value>Malagasy Ariary</value></option></options></item><item><options><option><code>reference_id</code><value>MWK</value></option><option><code>label</code><value>Malawi Kwacha</value></option><option><code>description</code><value>Malawi Kwacha</value></option></options></item><item><options><option><code>reference_id</code><value>MYR</value></option><option><code>label</code><value>Malaysian Ringgit</value></option><option><code>description</code><value>Malaysian Ringgit</value></option></options></item><item><options><option><code>reference_id</code><value>MVR</value></option><option><code>label</code><value>Rufiyaa</value></option><option><code>description</code><value>Rufiyaa</value></option></options></item><item><options><option><code>reference_id</code><value>MRU</value></option><option><code>label</code><value>Ouguiya</value></option><option><code>description</code><value>Ouguiya</value></option></options></item><item><options><option><code>reference_id</code><value>MUR</value></option><option><code>label</code><value>Mauritius Rupee</value></option><option><code>description</code><value>Mauritius Rupee</value></option></options></item><item><options><option><code>reference_id</code><value>XUA</value></option><option><code>label</code><value>ADB Unit of Account</value></option><option><code>description</code><value>ADB Unit of Account</value></option></options></item><item><options><option><code>reference_id</code><value>MXN</value></option><option><code>label</code><value>Mexican Peso</value></option><option><code>description</code><value>Mexican Peso</value></option></options></item><item><options><option><code>reference_id</code><value>MXV</value></option><option><code>label</code><value>Mexican Unidad de Inversion (UDI)</value></option><option><code>description</code><value>Mexican Unidad de Inversion (UDI)</value></option></options></item><item><options><option><code>reference_id</code><value>MDL</value></option><option><code>label</code><value>Moldovan Leu</value></option><option><code>description</code><value>Moldovan Leu</value></option></options></item><item><options><option><code>reference_id</code><value>MNT</value></option><option><code>label</code><value>Tugrik</value></option><option><code>description</code><value>Tugrik</value></option></options></item><item><options><option><code>reference_id</code><value>MAD</value></option><option><code>label</code><value>Moroccan Dirham</value></option><option><code>description</code><value>Moroccan Dirham</value></option></options></item><item><options><option><code>reference_id</code><value>MZN</value></option><option><code>label</code><value>Mozambique Metical</value></option><option><code>description</code><value>Mozambique Metical</value></option></options></item><item><options><option><code>reference_id</code><value>MMK</value></option><option><code>label</code><value>Kyat</value></option><option><code>description</code><value>Kyat</value></option></options></item><item><options><option><code>reference_id</code><value>NAD</value></option><option><code>label</code><value>Namibia Dollar</value></option><option><code>description</code><value>Namibia Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>NPR</value></option><option><code>label</code><value>Nepalese Rupee</value></option><option><code>description</code><value>Nepalese Rupee</value></option></options></item><item><options><option><code>reference_id</code><value>NIO</value></option><option><code>label</code><value>Cordoba Oro</value></option><option><code>description</code><value>Cordoba Oro</value></option></options></item><item><options><option><code>reference_id</code><value>NGN</value></option><option><code>label</code><value>Naira</value></option><option><code>description</code><value>Naira</value></option></options></item><item><options><option><code>reference_id</code><value>OMR</value></option><option><code>label</code><value>Rial Omani</value></option><option><code>description</code><value>Rial Omani</value></option></options></item><item><options><option><code>reference_id</code><value>PKR</value></option><option><code>label</code><value>Pakistan Rupee</value></option><option><code>description</code><value>Pakistan Rupee</value></option></options></item><item><options><option><code>reference_id</code><value>PAB</value></option><option><code>label</code><value>Balboa</value></option><option><code>description</code><value>Balboa</value></option></options></item><item><options><option><code>reference_id</code><value>PGK</value></option><option><code>label</code><value>Kina</value></option><option><code>description</code><value>Kina</value></option></options></item><item><options><option><code>reference_id</code><value>PYG</value></option><option><code>label</code><value>Guarani</value></option><option><code>description</code><value>Guarani</value></option></options></item><item><options><option><code>reference_id</code><value>PEN</value></option><option><code>label</code><value>Sol</value></option><option><code>description</code><value>Sol</value></option></options></item><item><options><option><code>reference_id</code><value>PHP</value></option><option><code>label</code><value>Philippine Peso</value></option><option><code>description</code><value>Philippine Peso</value></option></options></item><item><options><option><code>reference_id</code><value>PLN</value></option><option><code>label</code><value>Zloty</value></option><option><code>description</code><value>Zloty</value></option></options></item><item><options><option><code>reference_id</code><value>QAR</value></option><option><code>label</code><value>Qatari Rial</value></option><option><code>description</code><value>Qatari Rial</value></option></options></item><item><options><option><code>reference_id</code><value>RON</value></option><option><code>label</code><value>Romanian Leu</value></option><option><code>description</code><value>Romanian Leu</value></option></options></item><item><options><option><code>reference_id</code><value>RUB</value></option><option><code>label</code><value>Russian Ruble</value></option><option><code>description</code><value>Russian Ruble</value></option></options></item><item><options><option><code>reference_id</code><value>RWF</value></option><option><code>label</code><value>Rwanda Franc</value></option><option><code>description</code><value>Rwanda Franc</value></option></options></item><item><options><option><code>reference_id</code><value>SHP</value></option><option><code>label</code><value>Saint Helena Pound</value></option><option><code>description</code><value>Saint Helena Pound</value></option></options></item><item><options><option><code>reference_id</code><value>WST</value></option><option><code>label</code><value>Tala</value></option><option><code>description</code><value>Tala</value></option></options></item><item><options><option><code>reference_id</code><value>STN</value></option><option><code>label</code><value>Dobra</value></option><option><code>description</code><value>Dobra</value></option></options></item><item><options><option><code>reference_id</code><value>SAR</value></option><option><code>label</code><value>Saudi Riyal</value></option><option><code>description</code><value>Saudi Riyal</value></option></options></item><item><options><option><code>reference_id</code><value>RSD</value></option><option><code>label</code><value>Serbian Dinar</value></option><option><code>description</code><value>Serbian Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>SCR</value></option><option><code>label</code><value>Seychelles Rupee</value></option><option><code>description</code><value>Seychelles Rupee</value></option></options></item><item><options><option><code>reference_id</code><value>SLL</value></option><option><code>label</code><value>Leone</value></option><option><code>description</code><value>Leone</value></option></options></item><item><options><option><code>reference_id</code><value>SGD</value></option><option><code>label</code><value>Singapore Dollar</value></option><option><code>description</code><value>Singapore Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>XSU</value></option><option><code>label</code><value>Sucre</value></option><option><code>description</code><value>Sucre</value></option></options></item><item><options><option><code>reference_id</code><value>SBD</value></option><option><code>label</code><value>Solomon Islands Dollar</value></option><option><code>description</code><value>Solomon Islands Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>SOS</value></option><option><code>label</code><value>Somali Shilling</value></option><option><code>description</code><value>Somali Shilling</value></option></options></item><item><options><option><code>reference_id</code><value>SSP</value></option><option><code>label</code><value>South Sudanese Pound</value></option><option><code>description</code><value>South Sudanese Pound</value></option></options></item><item><options><option><code>reference_id</code><value>LKR</value></option><option><code>label</code><value>Sri Lanka Rupee</value></option><option><code>description</code><value>Sri Lanka Rupee</value></option></options></item><item><options><option><code>reference_id</code><value>SDG</value></option><option><code>label</code><value>Sudanese Pound</value></option><option><code>description</code><value>Sudanese Pound</value></option></options></item><item><options><option><code>reference_id</code><value>SRD</value></option><option><code>label</code><value>Surinam Dollar</value></option><option><code>description</code><value>Surinam Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>SEK</value></option><option><code>label</code><value>Swedish Krona</value></option><option><code>description</code><value>Swedish Krona</value></option></options></item><item><options><option><code>reference_id</code><value>CHE</value></option><option><code>label</code><value>WIR Euro</value></option><option><code>description</code><value>WIR Euro</value></option></options></item><item><options><option><code>reference_id</code><value>CHW</value></option><option><code>label</code><value>WIR Franc</value></option><option><code>description</code><value>WIR Franc</value></option></options></item><item><options><option><code>reference_id</code><value>SYP</value></option><option><code>label</code><value>Syrian Pound</value></option><option><code>description</code><value>Syrian Pound</value></option></options></item><item><options><option><code>reference_id</code><value>TWD</value></option><option><code>label</code><value>New Taiwan Dollar</value></option><option><code>description</code><value>New Taiwan Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>TJS</value></option><option><code>label</code><value>Somoni</value></option><option><code>description</code><value>Somoni</value></option></options></item><item><options><option><code>reference_id</code><value>TZS</value></option><option><code>label</code><value>Tanzanian Shilling</value></option><option><code>description</code><value>Tanzanian Shilling</value></option></options></item><item><options><option><code>reference_id</code><value>THB</value></option><option><code>label</code><value>Baht</value></option><option><code>description</code><value>Baht</value></option></options></item><item><options><option><code>reference_id</code><value>TOP</value></option><option><code>label</code><value>Pa’anga</value></option><option><code>description</code><value>Pa’anga</value></option></options></item><item><options><option><code>reference_id</code><value>TTD</value></option><option><code>label</code><value>Trinidad and Tobago Dollar</value></option><option><code>description</code><value>Trinidad and Tobago Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>TND</value></option><option><code>label</code><value>Tunisian Dinar</value></option><option><code>description</code><value>Tunisian Dinar</value></option></options></item><item><options><option><code>reference_id</code><value>TRY</value></option><option><code>label</code><value>Turkish Lira</value></option><option><code>description</code><value>Turkish Lira</value></option></options></item><item><options><option><code>reference_id</code><value>TMT</value></option><option><code>label</code><value>Turkmenistan New Manat</value></option><option><code>description</code><value>Turkmenistan New Manat</value></option></options></item><item><options><option><code>reference_id</code><value>UGX</value></option><option><code>label</code><value>Uganda Shilling</value></option><option><code>description</code><value>Uganda Shilling</value></option></options></item><item><options><option><code>reference_id</code><value>UAH</value></option><option><code>label</code><value>Hryvnia</value></option><option><code>description</code><value>Hryvnia</value></option></options></item><item><options><option><code>reference_id</code><value>AED</value></option><option><code>label</code><value>UAE Dirham</value></option><option><code>description</code><value>UAE Dirham</value></option></options></item><item><options><option><code>reference_id</code><value>USN</value></option><option><code>label</code><value>US Dollar (Next day)</value></option><option><code>description</code><value>US Dollar (Next day)</value></option></options></item><item><options><option><code>reference_id</code><value>UYU</value></option><option><code>label</code><value>Peso Uruguayo</value></option><option><code>description</code><value>Peso Uruguayo</value></option></options></item><item><options><option><code>reference_id</code><value>UYI</value></option><option><code>label</code><value>Uruguay Peso en Unidades Indexadas (UI)</value></option><option><code>description</code><value>Uruguay Peso en Unidades Indexadas (UI)</value></option></options></item><item><options><option><code>reference_id</code><value>UYW</value></option><option><code>label</code><value>Unidad Previsional</value></option><option><code>description</code><value>Unidad Previsional</value></option></options></item><item><options><option><code>reference_id</code><value>UZS</value></option><option><code>label</code><value>Uzbekistan Sum</value></option><option><code>description</code><value>Uzbekistan Sum</value></option></options></item><item><options><option><code>reference_id</code><value>VUV</value></option><option><code>label</code><value>Vatu</value></option><option><code>description</code><value>Vatu</value></option></options></item><item><options><option><code>reference_id</code><value>VES</value></option><option><code>label</code><value>Bolívar Soberano</value></option><option><code>description</code><value>Bolívar Soberano</value></option></options></item><item><options><option><code>reference_id</code><value>VND</value></option><option><code>label</code><value>Dong</value></option><option><code>description</code><value>Dong</value></option></options></item><item><options><option><code>reference_id</code><value>YER</value></option><option><code>label</code><value>Yemeni Rial</value></option><option><code>description</code><value>Yemeni Rial</value></option></options></item><item><options><option><code>reference_id</code><value>ZMW</value></option><option><code>label</code><value>Zambian Kwacha</value></option><option><code>description</code><value>Zambian Kwacha</value></option></options></item><item><options><option><code>reference_id</code><value>ZWL</value></option><option><code>label</code><value>Zimbabwe Dollar</value></option><option><code>description</code><value>Zimbabwe Dollar</value></option></options></item><item><options><option><code>reference_id</code><value>XBA</value></option><option><code>label</code><value>Bond Markets Unit European Composite Unit (EURCO)</value></option><option><code>description</code><value>Bond Markets Unit European Composite Unit (EURCO)</value></option></options></item><item><options><option><code>reference_id</code><value>XBB</value></option><option><code>label</code><value>Bond Markets Unit European Monetary Unit (E.M.U.-6)</value></option><option><code>description</code><value>Bond Markets Unit European Monetary Unit (E.M.U.-6)</value></option></options></item><item><options><option><code>reference_id</code><value>XBC</value></option><option><code>label</code><value>Bond Markets Unit European Unit of Account 9 (E.U.A.-9)</value></option><option><code>description</code><value>Bond Markets Unit European Unit of Account 9 (E.U.A.-9)</value></option></options></item><item><options><option><code>reference_id</code><value>XBD</value></option><option><code>label</code><value>Bond Markets Unit European Unit of Account 17 (E.U.A.-17)</value></option><option><code>description</code><value>Bond Markets Unit European Unit of Account 17 (E.U.A.-17)</value></option></options></item><item><options><option><code>reference_id</code><value>XTS</value></option><option><code>label</code><value>Codes specifically reserved for testing purposes</value></option><option><code>description</code><value>Codes specifically reserved for testing purposes</value></option></options></item><item><options><option><code>reference_id</code><value>XXX</value></option><option><code>label</code><value>The codes assigned for transactions where no currency is involved</value></option><option><code>description</code><value>The codes assigned for transactions where no currency is involved</value></option></options></item><item><options><option><code>reference_id</code><value>XAU</value></option><option><code>label</code><value>Gold</value></option><option><code>description</code><value>Gold</value></option></options></item><item><options><option><code>reference_id</code><value>XPD</value></option><option><code>label</code><value>Palladium</value></option><option><code>description</code><value>Palladium</value></option></options></item><item><options><option><code>reference_id</code><value>XPT</value></option><option><code>label</code><value>Platinum</value></option><option><code>description</code><value>Platinum</value></option></options></item><item><options><option><code>reference_id</code><value>XAG</value></option><option><code>label</code><value>Silver</value></option><option><code>description</code><value>Silver</value></option></options></item></items></static></sources></definition>', 1, '2023-11-16 17:17:40.882679', '2024-04-25 13:47:09.556604', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('67ddb1bf-0b35-4e14-bfb8-be8139a47096', 'Select format(s)', 'Select format(s)', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields/><sources><api><key>eestore.paas2.uninett.no</key><label>eestore.paas2.uninett.no</label><ordinal>0</ordinal><type>0</type><contentType>application/json</contentType><firstPage>1</firstPage><requestHttpMethod>0</requestHttpMethod><queries><query><cases><case><value>{like}</value></case></cases><defaultValue></defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>label</code><responsePath>name</responsePath></field><field><code>reference_id</code><responsePath>pid</responsePath></field></fieldsMapping><resultsArrayPath>$[''data''][*][''attributes'']</resultsArrayPath></results><url>https://eestore.paas2.uninett.no/api/fileformat/</url></api></sources></definition>', 1, '2024-10-14 10:15:06.722425', '2024-10-14 10:15:06.725424', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('6be585bc-9abe-4f92-8be3-5ad067d0b190', 'Metadata Services', 'Metadata Services', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields/><sources><api><key>eestore.paas2.uninett.no</key><label>eestore.paas2.uninett.no</label><ordinal>0</ordinal><type>0</type><contentType>application/json</contentType><firstPage>1</firstPage><requestHttpMethod>0</requestHttpMethod><queries><query><cases><case><value>{like}</value></case></cases><defaultValue></defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>label</code><responsePath>name</responsePath></field><field><code>reference_id</code><responsePath>pid</responsePath></field></fieldsMapping><resultsArrayPath>$[''data''][*][''attributes'']</resultsArrayPath></results><url>https://eestore.paas2.uninett.no/api/datasetmdrepo/</url></api></sources></definition>', 1, '2024-10-14 10:15:06.615425', '2024-10-14 10:15:06.623425', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('7eeffb98-58fb-4921-82ec-e27f32f8e738', 'Organisations', 'organisations', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="pid" label="Pid Organisations" description="Pid Organisations" dataType="Text"/><field code="pidTypeField" label="PidType Organisations" description="PidType Organisations" dataType="Text"/></fields><sources><api><key>openaire</key><label>OpenAIRE</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json; charset=utf-8</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue></defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>header.originalId.first()</responsePath></field><field><code>label</code><responsePath>organization.legalname</responsePath></field><field><code>description</code><responsePath>organization.legalname</responsePath></field><field><code>pid</code><responsePath>pid.first().value</responsePath></field><field><code>pidTypeField</code><responsePath>pid.first().typeCode</responsePath></field></fieldsMapping><resultsArrayPath>$[''results''][*]</resultsArrayPath></results><url>https://services.openaire.eu/search/v2/api/organizations/?q={like}&amp;page={page}&amp;size={pageSize}&amp;format=json</url></api></sources></definition>', 1, '2023-11-17 10:13:15.873808', '2025-05-16 09:45:55.160523', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('538928bb-c7c6-452e-b66d-08e539f5f082', 'Funders', 'funders', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><api><key>openaire</key><label>OpenAIRE</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>1</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json; charset=utf-8</contentType><filterType>local</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[*]</paginationPath><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>name</responsePath></field><field><code>description</code><responsePath>name</responsePath></field></fieldsMapping><resultsArrayPath>$[''refineResults''][''relfunder''][*]</resultsArrayPath></results><url>https://beta.services.openaire.eu/search/v2/api/results?&amp;refine=true&amp;fields=relfunder&amp;page={page}&amp;size=0&amp;format=json&amp;sf=relfunder</url></api><api><key>openaire projects</key><label>OpenAIRE Projects</label><ordinal>2</ordinal><type>0</type><authentication><enabled>false</enabled></authentication><contentType>application/json; charset=utf-8</contentType><filterType>local</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[*]</paginationPath><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>name</responsePath></field><field><code>description</code><responsePath>name</responsePath></field></fieldsMapping><resultsArrayPath>$[''refineResults''][''funder''][*]</resultsArrayPath></results><url>https://beta.services.openaire.eu/search/v2/api/projects?&amp;refine=true&amp;fields=funder&amp;page={page}&amp;size=0&amp;format=json&amp;sf=funder</url></api></sources></definition>', 1, '2023-11-16 11:22:57.942097', '2025-05-20 07:42:32.881862', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('d26b64ba-fda0-4b14-b3ef-9856a492d55a', 'Repositories', 'repositories', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><api><key>openaire</key><label>OpenAIRE</label><ordinal>1</ordinal><referenceTypeDependencyIds/><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>1</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><name>like</name></query></queries><requestBody>{"officialname": "{like}", "typology":"data"}</requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>id</responsePath></field><field><code>label</code><responsePath>officialname</responsePath></field><field><code>description</code><responsePath>officialname</responsePath></field></fieldsMapping><resultsArrayPath>$[''datasourceInfo''][*]</resultsArrayPath></results><url>https://services.openaire.eu/openaire/ds/searchdetails/{page}/{pageSize}?requestSortBy=id&amp;order=ASCENDING</url></api></sources></definition>', 1, '2023-11-16 17:10:33.849688', '2024-02-16 12:45:18.66016', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('5b9c284f-f041-4995-96cc-fad7ad13289c', 'Grants', 'grants', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><sources><api><key>openaire</key><label>OpenAIRE</label><ordinal>1</ordinal><referenceTypeDependencyIds><referenceTypeDependencyIds>538928bb-c7c6-452e-b66d-08e539f5f082</referenceTypeDependencyIds></referenceTypeDependencyIds><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json; charset=utf-8</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue>*</defaultValue><name>like</name></query><query><cases><case><referenceTypeId>538928bb-c7c6-452e-b66d-08e539f5f082</referenceTypeId><referenceTypeSourceKey>openaire</referenceTypeSourceKey><value>&amp;fq=(funder exact "{reference}")</value></case></cases><defaultValue></defaultValue><name>funderQuery</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>header.originalId.first()</responsePath></field><field><code>label</code><responsePath>@{{project.title}} (@{{header.originalId[0]}})</responsePath></field><field><code>description</code><responsePath>project.title</responsePath></field></fieldsMapping><resultsArrayPath>$[''results''][*]</resultsArrayPath></results><url>https://services.openaire.eu/search/v2/api/resources?query=((oaftype exact project) and ((projectcode_nt exact "{like}")or("{like}"))){funderQuery}&amp;page={page}&amp;size={pageSize}&amp;format=json</url></api></sources></definition>', 1, '2023-11-16 16:41:53.106646', '2025-05-19 08:42:38.085758', NULL);
INSERT INTO public."ReferenceType" (id, name, code, definition, is_active, created_at, updated_at, tenant) VALUES ('51225b6a-86a6-48ac-9192-f15096dbcb8a', 'Publications', 'publications', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><definition><fields><field code="pid" label="pid" description="pid" dataType="Text"/><field code="pidTypeField" label="pidTypeField" description="pidTypeField" dataType="Text"/></fields><sources><api><key>openaire</key><label>OpenAIRE Publications</label><ordinal>1</ordinal><type>0</type><authentication><authUrlMethod>0</authUrlMethod><authUrlBody></authUrlBody><authTokenPath></authTokenPath><authUrl></authUrl><enabled>false</enabled><authType></authType></authentication><contentType>application/json;charset=UTF-8</contentType><filterType>remote</filterType><firstPage>0</firstPage><requestHttpMethod>0</requestHttpMethod><paginationPath>$[''meta''][''pagination''][''page'',''pages'',''count'']</paginationPath><queries><query><cases><case><value>{like}</value></case></cases><defaultValue>*</defaultValue><name>like</name></query></queries><requestBody></requestBody><results><fieldsMapping><field><code>reference_id</code><responsePath>header.originalId.first()</responsePath></field><field><code>label</code><responsePath>result.maintitle</responsePath></field><field><code>description</code><responsePath>result.maintitle</responsePath></field><field><code>pid</code><responsePath>result.instance.first().pid.first().value</responsePath></field><field><code>pidTypeField</code><responsePath>result.instance.first().pid.first().typeCode</responsePath></field></fieldsMapping><resultsArrayPath>$[''results''][*]</resultsArrayPath></results><url>https://services.openaire.eu/search/v2/api/resources?query=oaftype exact result and "{like}"&amp;page={page}&amp;size={pageSize}&amp;format=json</url></api></sources></definition>', 1, '2023-11-16 13:07:20.591433', '2025-05-16 09:59:29.249724', NULL);


--
-- Data for Name: StorageFile; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: SupportiveMaterial; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: Tag; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: Tenant; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: TenantConfiguration; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: TenantUser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: UsageLimit; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: User; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: UserContactInfo; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: UserCredential; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: UserDescriptionTemplate; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: UserRole; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: UserSettings; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_Annotation; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_AnnotationStatus; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_EntityUser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_QueueInbox; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_QueueOutbox; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_Status; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_Tenant; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_TenantUser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_User; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ant_UserCredential; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_InAppNotification; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_Notification; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_NotificationTemplate; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_QueueInbox; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_QueueOutbox; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_Tenant; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_TenantConfiguration; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_TenantUser; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_User; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_UserContactInfo; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_UserCredential; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: ntf_UserNotificationPreference; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: ActionConfirmation ActionConfirmation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ActionConfirmation"
    ADD CONSTRAINT "ActionConfirmation_pkey" PRIMARY KEY (id);


--
-- Name: DescriptionTemplate DatasetProfile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplate"
    ADD CONSTRAINT "DatasetProfile_pkey" PRIMARY KEY (id);


--
-- Name: DescriptionReference DescriptionReference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_pkey" PRIMARY KEY (id);


--
-- Name: DescriptionStatus DescriptionStatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionStatus"
    ADD CONSTRAINT "DescriptionStatus_pkey" PRIMARY KEY (id);


--
-- Name: DescriptionTag DescriptionTag_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_pkey" PRIMARY KEY (id);


--
-- Name: DescriptionTemplateType DescriptionTemplateType_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplateType"
    ADD CONSTRAINT "DescriptionTemplateType_pkey" PRIMARY KEY (id);


--
-- Name: DescriptionWorkflow DescriptionWorkflow_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionWorkflow"
    ADD CONSTRAINT "DescriptionWorkflow_pkey" PRIMARY KEY (id);


--
-- Name: Description Description_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_pkey" PRIMARY KEY (id);


--
-- Name: EntityDoi Doi_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."EntityDoi"
    ADD CONSTRAINT "Doi_pkey" PRIMARY KEY (id);


--
-- Name: Evaluation Evaluation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Evaluation"
    ADD CONSTRAINT "Evaluation_pkey" PRIMARY KEY (id);


--
-- Name: Language Language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Language"
    ADD CONSTRAINT "Language_pkey" PRIMARY KEY (id);


--
-- Name: Lock Lock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Lock"
    ADD CONSTRAINT "Lock_pkey" PRIMARY KEY (id);


--
-- Name: ntf_NotificationTemplate NotificationTemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_NotificationTemplate"
    ADD CONSTRAINT "NotificationTemplate_pkey" PRIMARY KEY (id);


--
-- Name: ntf_User Ntf_User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_User"
    ADD CONSTRAINT "Ntf_User_pkey" PRIMARY KEY (id);


--
-- Name: PlanBlueprint PlanBlueprint_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanBlueprint"
    ADD CONSTRAINT "PlanBlueprint_pkey" PRIMARY KEY (id);


--
-- Name: PlanDescriptionTemplate PlanDescriptionTemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanDescriptionTemplate"
    ADD CONSTRAINT "PlanDescriptionTemplate_pkey" PRIMARY KEY (id);


--
-- Name: PlanReference PlanReference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_pkey" PRIMARY KEY (id);


--
-- Name: PlanStatus PlanStatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanStatus"
    ADD CONSTRAINT "PlanStatus_pkey" PRIMARY KEY (id);


--
-- Name: PlanUser PlanUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_pkey" PRIMARY KEY (id);


--
-- Name: PlanWorkflow PlanWorkflow_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanWorkflow"
    ADD CONSTRAINT "PlanWorkflow_pkey" PRIMARY KEY (id);


--
-- Name: Plan Plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_pkey" PRIMARY KEY (id);


--
-- Name: PrefillingSource PrefillingSource_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PrefillingSource"
    ADD CONSTRAINT "PrefillingSource_pkey" PRIMARY KEY (id);


--
-- Name: QueueInbox QueryInbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueInbox"
    ADD CONSTRAINT "QueryInbox_pkey" PRIMARY KEY (id);


--
-- Name: QueueOutbox QueueOutbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueOutbox"
    ADD CONSTRAINT "QueueOutbox_pkey" PRIMARY KEY (id);


--
-- Name: ReferenceType ReferenceType_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ReferenceType"
    ADD CONSTRAINT "ReferenceType_pkey" PRIMARY KEY (id);


--
-- Name: Reference Reference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_pkey" PRIMARY KEY (id);


--
-- Name: StorageFile StorageFile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."StorageFile"
    ADD CONSTRAINT "StorageFile_pkey" PRIMARY KEY (id);


--
-- Name: SupportiveMaterial SupportiveMaterial_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SupportiveMaterial"
    ADD CONSTRAINT "SupportiveMaterial_pkey" PRIMARY KEY (id);


--
-- Name: Tag Tag_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tag"
    ADD CONSTRAINT "Tag_pkey" PRIMARY KEY (id);


--
-- Name: TenantConfiguration TenantConfiguration_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantConfiguration"
    ADD CONSTRAINT "TenantConfiguration_pkey" PRIMARY KEY (id);


--
-- Name: TenantUser TenantUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantUser"
    ADD CONSTRAINT "TenantUser_pkey" PRIMARY KEY (id);


--
-- Name: Tenant Tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tenant"
    ADD CONSTRAINT "Tenant_pkey" PRIMARY KEY (id);


--
-- Name: UsageLimit UsageLimit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UsageLimit"
    ADD CONSTRAINT "UsageLimit_pkey" PRIMARY KEY (id);


--
-- Name: UserContactInfo UserContactInfo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserContactInfo"
    ADD CONSTRAINT "UserContactInfo_pkey" PRIMARY KEY (id);


--
-- Name: UserCredential UserCredential_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserCredential"
    ADD CONSTRAINT "UserCredential_pkey" PRIMARY KEY (id);


--
-- Name: UserDescriptionTemplate UserDatasetProfile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDatasetProfile_pkey" PRIMARY KEY (id);


--
-- Name: UserRole UserRole_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserRole"
    ADD CONSTRAINT "UserRole_pkey" PRIMARY KEY (id);


--
-- Name: User User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (id);


--
-- Name: ant_AnnotationStatus ant_AnnotationStatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_pkey" PRIMARY KEY (id);


--
-- Name: ant_Annotation ant_Annotation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_pkey" PRIMARY KEY (id);


--
-- Name: ant_EntityUser ant_EntityUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_EntityUser"
    ADD CONSTRAINT "ant_EntityUser_pkey" PRIMARY KEY (id);


--
-- Name: ant_QueueInbox ant_QueryInbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueInbox"
    ADD CONSTRAINT "ant_QueryInbox_pkey" PRIMARY KEY (id);


--
-- Name: ant_QueueOutbox ant_QueueOutbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueOutbox"
    ADD CONSTRAINT "ant_QueueOutbox_pkey" PRIMARY KEY (id);


--
-- Name: ant_Status ant_Status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Status"
    ADD CONSTRAINT "ant_Status_pkey" PRIMARY KEY (id);


--
-- Name: ant_TenantUser ant_TenantUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_TenantUser"
    ADD CONSTRAINT "ant_TenantUser_pkey" PRIMARY KEY (id);


--
-- Name: ant_Tenant ant_Tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Tenant"
    ADD CONSTRAINT "ant_Tenant_pkey" PRIMARY KEY (id);


--
-- Name: ant_UserCredential ant_UserCredential_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_UserCredential"
    ADD CONSTRAINT "ant_UserCredential_pkey" PRIMARY KEY (id);


--
-- Name: ant_User ant_User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_User"
    ADD CONSTRAINT "ant_User_pkey" PRIMARY KEY (id);


--
-- Name: ntf_InAppNotification ntf_InAppNotification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_InAppNotification"
    ADD CONSTRAINT "ntf_InAppNotification_pkey" PRIMARY KEY (id);


--
-- Name: ntf_Notification ntf_Notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Notification"
    ADD CONSTRAINT "ntf_Notification_pkey" PRIMARY KEY (id);


--
-- Name: ntf_QueueInbox ntf_QueryInbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueInbox"
    ADD CONSTRAINT "ntf_QueryInbox_pkey" PRIMARY KEY (id);


--
-- Name: ntf_QueueOutbox ntf_QueueOutbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueOutbox"
    ADD CONSTRAINT "ntf_QueueOutbox_pkey" PRIMARY KEY (id);


--
-- Name: ntf_TenantConfiguration ntf_TenantConfguration_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantConfiguration"
    ADD CONSTRAINT "ntf_TenantConfguration_pkey" PRIMARY KEY (id);


--
-- Name: ntf_TenantUser ntf_TenantUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantUser"
    ADD CONSTRAINT "ntf_TenantUser_pkey" PRIMARY KEY (id);


--
-- Name: ntf_Tenant ntf_Tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Tenant"
    ADD CONSTRAINT "ntf_Tenant_pkey" PRIMARY KEY (id);


--
-- Name: ntf_UserContactInfo ntf_UserContactInfo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserContactInfo"
    ADD CONSTRAINT "ntf_UserContactInfo_pkey" PRIMARY KEY (id);


--
-- Name: ntf_UserCredential ntf_UserCredential_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserCredential"
    ADD CONSTRAINT "ntf_UserCredential_pkey" PRIMARY KEY (id);


--
-- Name: ntf_UserNotificationPreference ntf_UserNotificationPreference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserNotificationPreference"
    ADD CONSTRAINT "ntf_UserNotificationPreference_pkey" PRIMARY KEY (id);


--
-- Name: UserSettings user_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSettings"
    ADD CONSTRAINT user_settings_pkey PRIMARY KEY (id);


--
-- Name: ActionConfirmation ActionConfirmation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ActionConfirmation"
    ADD CONSTRAINT "ActionConfirmation_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id);


--
-- Name: ActionConfirmation ActionConfirmation_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ActionConfirmation"
    ADD CONSTRAINT "ActionConfirmation_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: DescriptionReference DescriptionReference_description_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_description_fkey" FOREIGN KEY (description_id) REFERENCES public."Description"(id);


--
-- Name: DescriptionReference DescriptionReference_reference_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_reference_fkey" FOREIGN KEY (reference_id) REFERENCES public."Reference"(id);


--
-- Name: DescriptionReference DescriptionReference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: DescriptionStatus DescriptionStatus_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionStatus"
    ADD CONSTRAINT "DescriptionStatus_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: DescriptionTag DescriptionTag_description_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_description_fkey" FOREIGN KEY (description) REFERENCES public."Description"(id);


--
-- Name: DescriptionTag DescriptionTag_tag_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_tag_fkey" FOREIGN KEY (tag) REFERENCES public."Tag"(id);


--
-- Name: DescriptionTag DescriptionTag_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: DescriptionTemplateType DescriptionTemplateType_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplateType"
    ADD CONSTRAINT "DescriptionTemplateType_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: DescriptionTemplate DescriptionTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplate"
    ADD CONSTRAINT "DescriptionTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: DescriptionTemplate DescriptionTemplate_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplate"
    ADD CONSTRAINT "DescriptionTemplate_type_fkey" FOREIGN KEY (type) REFERENCES public."DescriptionTemplateType"(id);


--
-- Name: DescriptionWorkflow DescriptionWorkflow_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionWorkflow"
    ADD CONSTRAINT "DescriptionWorkflow_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: Description Description_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id) NOT VALID;


--
-- Name: Description Description_description_template_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_description_template_fkey" FOREIGN KEY (description_template) REFERENCES public."DescriptionTemplate"(id) NOT VALID;


--
-- Name: Description Description_plan_description_template_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_plan_description_template_fkey" FOREIGN KEY (plan_description_template) REFERENCES public."PlanDescriptionTemplate"(id) NOT VALID;


--
-- Name: Description Description_plan_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_plan_fkey" FOREIGN KEY (plan) REFERENCES public."Plan"(id);


--
-- Name: Description Description_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_status_fkey" FOREIGN KEY (status) REFERENCES public."DescriptionStatus"(id) NOT VALID;


--
-- Name: Description Description_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: EntityDoi EntityDoi_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."EntityDoi"
    ADD CONSTRAINT "EntityDoi_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: Evaluation Evaluation_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Evaluation"
    ADD CONSTRAINT "Evaluation_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: Evaluation Evaluation_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Evaluation"
    ADD CONSTRAINT "Evaluation_user_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id);


--
-- Name: Language Language_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Language"
    ADD CONSTRAINT "Language_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: Lock Lock_lockedby_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Lock"
    ADD CONSTRAINT "Lock_lockedby_fkey" FOREIGN KEY (locked_by) REFERENCES public."User"(id);


--
-- Name: Lock Lock_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Lock"
    ADD CONSTRAINT "Lock_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: ntf_NotificationTemplate NotificationTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_NotificationTemplate"
    ADD CONSTRAINT "NotificationTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: PlanBlueprint PlanBlueprint_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanBlueprint"
    ADD CONSTRAINT "PlanBlueprint_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: PlanDescriptionTemplate PlanDescriptionTemplate_plan_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanDescriptionTemplate"
    ADD CONSTRAINT "PlanDescriptionTemplate_plan_fkey" FOREIGN KEY (plan) REFERENCES public."Plan"(id);


--
-- Name: PlanDescriptionTemplate PlanDescriptionTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanDescriptionTemplate"
    ADD CONSTRAINT "PlanDescriptionTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: PlanReference PlanReference_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_plan_id_fkey" FOREIGN KEY (plan_id) REFERENCES public."Plan"(id);


--
-- Name: PlanReference PlanReference_reference_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_reference_id_fkey" FOREIGN KEY (reference_id) REFERENCES public."Reference"(id);


--
-- Name: PlanReference PlanReference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: PlanStatus PlanStatus_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanStatus"
    ADD CONSTRAINT "PlanStatus_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: PlanUser PlanUser_plan_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_plan_fkey" FOREIGN KEY (plan) REFERENCES public."Plan"(id);


--
-- Name: PlanUser PlanUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: PlanUser PlanUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id) NOT VALID;


--
-- Name: PlanWorkflow PlanWorkflow_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanWorkflow"
    ADD CONSTRAINT "PlanWorkflow_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: Plan Plan_blueprint_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_blueprint_fkey" FOREIGN KEY (blueprint) REFERENCES public."PlanBlueprint"(id);


--
-- Name: Plan Plan_creator_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_creator_fkey" FOREIGN KEY (creator) REFERENCES public."User"(id) NOT VALID;


--
-- Name: Plan Plan_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_status_fkey" FOREIGN KEY (status) REFERENCES public."PlanStatus"(id) NOT VALID;


--
-- Name: Plan Plan_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: PrefillingSource PrefillingSource_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PrefillingSource"
    ADD CONSTRAINT "PrefillingSource_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: QueueInbox QueryInbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueInbox"
    ADD CONSTRAINT "QueryInbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: QueueOutbox QueueOutbox_tennat_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueOutbox"
    ADD CONSTRAINT "QueueOutbox_tennat_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: ReferenceType ReferenceType_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ReferenceType"
    ADD CONSTRAINT "ReferenceType_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: Reference Reference_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id) NOT VALID;


--
-- Name: Reference Reference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: Reference Reference_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_type_fkey" FOREIGN KEY (type) REFERENCES public."ReferenceType"(id);


--
-- Name: StorageFile StorageFile_owner_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."StorageFile"
    ADD CONSTRAINT "StorageFile_owner_fkey" FOREIGN KEY (owner) REFERENCES public."User"(id);


--
-- Name: StorageFile StorageFile_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."StorageFile"
    ADD CONSTRAINT "StorageFile_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: SupportiveMaterial SupportiveMaterial_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SupportiveMaterial"
    ADD CONSTRAINT "SupportiveMaterial_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: Tag Tag_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tag"
    ADD CONSTRAINT "Tag_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id) NOT VALID;


--
-- Name: Tag Tag_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tag"
    ADD CONSTRAINT "Tag_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: TenantConfiguration TenantConfiguration_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantConfiguration"
    ADD CONSTRAINT "TenantConfiguration_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: TenantUser TenantUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantUser"
    ADD CONSTRAINT "TenantUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: TenantUser TenantUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantUser"
    ADD CONSTRAINT "TenantUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id);


--
-- Name: UsageLimit UsageLimit_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UsageLimit"
    ADD CONSTRAINT "UsageLimit_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- Name: UserContactInfo UserContactInfo_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserContactInfo"
    ADD CONSTRAINT "UserContactInfo_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id);


--
-- Name: UserCredential UserCredential_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserCredential"
    ADD CONSTRAINT "UserCredential_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id);


--
-- Name: UserDescriptionTemplate UserDatasetProfile_datasetProfile_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDatasetProfile_datasetProfile_fkey" FOREIGN KEY (description_template) REFERENCES public."DescriptionTemplate"(id);


--
-- Name: UserDescriptionTemplate UserDescriptionTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDescriptionTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: UserDescriptionTemplate UserDescriptionTemplate_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDescriptionTemplate_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id) NOT VALID;


--
-- Name: UserRole UserRole_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserRole"
    ADD CONSTRAINT "UserRole_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: UserRole UserRole_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserRole"
    ADD CONSTRAINT "UserRole_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id) NOT VALID;


--
-- Name: UserSettings UserSettings_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSettings"
    ADD CONSTRAINT "UserSettings_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- Name: ant_AnnotationStatus ant_AnnotationStatus_annotation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_annotation_fkey" FOREIGN KEY (annotation_id) REFERENCES public."ant_Annotation"(id);


--
-- Name: ant_AnnotationStatus ant_AnnotationStatus_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_status_fkey" FOREIGN KEY (status_id) REFERENCES public."ant_Status"(id);


--
-- Name: ant_AnnotationStatus ant_AnnotationStatus_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- Name: ant_Annotation ant_Annotation_parent_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_parent_fkey" FOREIGN KEY (parent_id) REFERENCES public."ant_Annotation"(id);


--
-- Name: ant_Annotation ant_Annotation_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- Name: ant_Annotation ant_Annotation_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_user_fkey" FOREIGN KEY (subject_id) REFERENCES public."ant_User"(id);


--
-- Name: ant_EntityUser ant_EntityUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_EntityUser"
    ADD CONSTRAINT "ant_EntityUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- Name: ant_EntityUser ant_EntityUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_EntityUser"
    ADD CONSTRAINT "ant_EntityUser_user_fkey" FOREIGN KEY (user_id) REFERENCES public."ant_User"(id);


--
-- Name: ant_QueueInbox ant_QueryInbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueInbox"
    ADD CONSTRAINT "ant_QueryInbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- Name: ant_QueueOutbox ant_QueueOutbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueOutbox"
    ADD CONSTRAINT "ant_QueueOutbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- Name: ant_Status ant_Status_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Status"
    ADD CONSTRAINT "ant_Status_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- Name: ant_TenantUser ant_TenantUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_TenantUser"
    ADD CONSTRAINT "ant_TenantUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- Name: ant_TenantUser ant_TenantUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_TenantUser"
    ADD CONSTRAINT "ant_TenantUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."ant_User"(id);


--
-- Name: ant_UserCredential ant_UserCredential_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_UserCredential"
    ADD CONSTRAINT "ant_UserCredential_user_fkey" FOREIGN KEY ("user") REFERENCES public."ant_User"(id);


--
-- Name: ntf_InAppNotification ntf_InAppNotification_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_InAppNotification"
    ADD CONSTRAINT "ntf_InAppNotification_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: ntf_InAppNotification ntf_InAppNotification_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_InAppNotification"
    ADD CONSTRAINT "ntf_InAppNotification_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- Name: ntf_Notification ntf_Notification_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Notification"
    ADD CONSTRAINT "ntf_Notification_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: ntf_Notification ntf_Notification_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Notification"
    ADD CONSTRAINT "ntf_Notification_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- Name: ntf_QueueInbox ntf_QueryInbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueInbox"
    ADD CONSTRAINT "ntf_QueryInbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: ntf_QueueOutbox ntf_QueueOutbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueOutbox"
    ADD CONSTRAINT "ntf_QueueOutbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: ntf_TenantConfiguration ntf_TenantConfiguration_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantConfiguration"
    ADD CONSTRAINT "ntf_TenantConfiguration_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: ntf_TenantUser ntf_TenantUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantUser"
    ADD CONSTRAINT "ntf_TenantUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: ntf_TenantUser ntf_TenantUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantUser"
    ADD CONSTRAINT "ntf_TenantUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- Name: ntf_UserContactInfo ntf_UserContactInfo_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserContactInfo"
    ADD CONSTRAINT "ntf_UserContactInfo_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- Name: ntf_UserCredential ntf_UserCredential_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserCredential"
    ADD CONSTRAINT "ntf_UserCredential_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- Name: ntf_UserNotificationPreference ntf_UserNotificationPreference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserNotificationPreference"
    ADD CONSTRAINT "ntf_UserNotificationPreference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- Name: ntf_UserNotificationPreference ntf_UserNotificationPreference_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserNotificationPreference"
    ADD CONSTRAINT "ntf_UserNotificationPreference_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- PostgreSQL database dump complete
--


