--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.3

-- Started on 2024-10-16 10:21:53

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 123976)
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- TOC entry 3897 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- TOC entry 909 (class 1247 OID 123988)
-- Name: doientitytype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.doientitytype AS ENUM (
    'DMP'
);


--
-- TOC entry 912 (class 1247 OID 123992)
-- Name: entitytype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.entitytype AS ENUM (
    'DATASET',
    'DMP'
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 216 (class 1259 OID 123997)
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
-- TOC entry 217 (class 1259 OID 124012)
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
-- TOC entry 218 (class 1259 OID 124083)
-- Name: Description; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Description" (
    id uuid NOT NULL,
    label character varying(250) NOT NULL,
    plan uuid NOT NULL,
    properties text,
    status smallint DEFAULT 0 NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_active smallint DEFAULT 1 NOT NULL,
    finalized_at timestamp without time zone,
    description_template uuid NOT NULL,
    created_by uuid NOT NULL,
    plan_description_template uuid NOT NULL,
    tenant uuid
);


--
-- TOC entry 219 (class 1259 OID 124092)
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
-- TOC entry 272 (class 1259 OID 162365)
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
    tenant uuid
);


--
-- TOC entry 220 (class 1259 OID 124100)
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
-- TOC entry 221 (class 1259 OID 124103)
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
-- TOC entry 3898 (class 0 OID 0)
-- Dependencies: 221
-- Name: TABLE "DescriptionTemplate"; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public."DescriptionTemplate" IS 'Profiles for dmp datasets';


--
-- TOC entry 222 (class 1259 OID 124112)
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
-- TOC entry 274 (class 1259 OID 162390)
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
-- TOC entry 228 (class 1259 OID 124167)
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
-- TOC entry 229 (class 1259 OID 124210)
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
-- TOC entry 230 (class 1259 OID 124215)
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
-- TOC entry 223 (class 1259 OID 124116)
-- Name: Plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."Plan" (
    id uuid NOT NULL,
    label character varying(250) NOT NULL,
    version integer NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
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
    tenant uuid
);


--
-- TOC entry 224 (class 1259 OID 124126)
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
    code character varying(200) NOT NULL
);


--
-- TOC entry 225 (class 1259 OID 124135)
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
-- TOC entry 226 (class 1259 OID 124141)
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
-- TOC entry 271 (class 1259 OID 162353)
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
    definition xml
);


--
-- TOC entry 227 (class 1259 OID 124149)
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
    section_id uuid
);


--
-- TOC entry 273 (class 1259 OID 162377)
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
-- TOC entry 231 (class 1259 OID 124232)
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
-- TOC entry 232 (class 1259 OID 124247)
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
-- TOC entry 233 (class 1259 OID 124252)
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
-- TOC entry 234 (class 1259 OID 124257)
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
-- TOC entry 235 (class 1259 OID 124262)
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
-- TOC entry 236 (class 1259 OID 124294)
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
-- TOC entry 237 (class 1259 OID 124299)
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
-- TOC entry 238 (class 1259 OID 124304)
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
-- TOC entry 239 (class 1259 OID 124309)
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
-- TOC entry 240 (class 1259 OID 124314)
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
-- TOC entry 241 (class 1259 OID 124319)
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
-- TOC entry 270 (class 1259 OID 151342)
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
-- TOC entry 242 (class 1259 OID 124322)
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
-- TOC entry 243 (class 1259 OID 124330)
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
-- TOC entry 244 (class 1259 OID 124336)
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
-- TOC entry 245 (class 1259 OID 124345)
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
-- TOC entry 246 (class 1259 OID 124354)
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
-- TOC entry 247 (class 1259 OID 124360)
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
-- TOC entry 248 (class 1259 OID 124368)
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
-- TOC entry 269 (class 1259 OID 150484)
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
-- TOC entry 249 (class 1259 OID 124373)
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
-- TOC entry 250 (class 1259 OID 124376)
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
-- TOC entry 251 (class 1259 OID 124381)
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
-- TOC entry 268 (class 1259 OID 150474)
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
-- TOC entry 252 (class 1259 OID 124386)
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
-- TOC entry 253 (class 1259 OID 124392)
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
-- TOC entry 254 (class 1259 OID 124395)
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
-- TOC entry 255 (class 1259 OID 124403)
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
-- TOC entry 256 (class 1259 OID 124408)
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
-- TOC entry 257 (class 1259 OID 124413)
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
-- TOC entry 258 (class 1259 OID 124418)
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
-- TOC entry 259 (class 1259 OID 124423)
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
-- TOC entry 260 (class 1259 OID 124428)
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
-- TOC entry 261 (class 1259 OID 124433)
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
-- TOC entry 262 (class 1259 OID 124439)
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
-- TOC entry 263 (class 1259 OID 124444)
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
-- TOC entry 264 (class 1259 OID 124447)
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
-- TOC entry 265 (class 1259 OID 124455)
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
-- TOC entry 266 (class 1259 OID 124462)
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
-- TOC entry 267 (class 1259 OID 124467)
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
-- TOC entry 3546 (class 2606 OID 124474)
-- Name: ActionConfirmation ActionConfirmation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ActionConfirmation"
    ADD CONSTRAINT "ActionConfirmation_pkey" PRIMARY KEY (id);


--
-- TOC entry 3554 (class 2606 OID 124490)
-- Name: DescriptionTemplate DatasetProfile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplate"
    ADD CONSTRAINT "DatasetProfile_pkey" PRIMARY KEY (id);


--
-- TOC entry 3550 (class 2606 OID 124498)
-- Name: DescriptionReference DescriptionReference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_pkey" PRIMARY KEY (id);


--
-- TOC entry 3656 (class 2606 OID 162371)
-- Name: DescriptionStatus DescriptionStatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionStatus"
    ADD CONSTRAINT "DescriptionStatus_pkey" PRIMARY KEY (id);


--
-- TOC entry 3552 (class 2606 OID 124500)
-- Name: DescriptionTag DescriptionTag_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_pkey" PRIMARY KEY (id);


--
-- TOC entry 3556 (class 2606 OID 124502)
-- Name: DescriptionTemplateType DescriptionTemplateType_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplateType"
    ADD CONSTRAINT "DescriptionTemplateType_pkey" PRIMARY KEY (id);


--
-- TOC entry 3660 (class 2606 OID 162396)
-- Name: DescriptionWorkflow DescriptionWorkflow_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionWorkflow"
    ADD CONSTRAINT "DescriptionWorkflow_pkey" PRIMARY KEY (id);


--
-- TOC entry 3548 (class 2606 OID 124504)
-- Name: Description Description_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_pkey" PRIMARY KEY (id);


--
-- TOC entry 3568 (class 2606 OID 124516)
-- Name: EntityDoi Doi_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."EntityDoi"
    ADD CONSTRAINT "Doi_pkey" PRIMARY KEY (id);


--
-- TOC entry 3570 (class 2606 OID 124528)
-- Name: Language Language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Language"
    ADD CONSTRAINT "Language_pkey" PRIMARY KEY (id);


--
-- TOC entry 3572 (class 2606 OID 124530)
-- Name: Lock Lock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Lock"
    ADD CONSTRAINT "Lock_pkey" PRIMARY KEY (id);


--
-- TOC entry 3628 (class 2606 OID 124536)
-- Name: ntf_NotificationTemplate NotificationTemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_NotificationTemplate"
    ADD CONSTRAINT "NotificationTemplate_pkey" PRIMARY KEY (id);


--
-- TOC entry 3640 (class 2606 OID 124538)
-- Name: ntf_User Ntf_User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_User"
    ADD CONSTRAINT "Ntf_User_pkey" PRIMARY KEY (id);


--
-- TOC entry 3560 (class 2606 OID 124482)
-- Name: PlanBlueprint PlanBlueprint_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanBlueprint"
    ADD CONSTRAINT "PlanBlueprint_pkey" PRIMARY KEY (id);


--
-- TOC entry 3562 (class 2606 OID 124506)
-- Name: PlanDescriptionTemplate PlanDescriptionTemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanDescriptionTemplate"
    ADD CONSTRAINT "PlanDescriptionTemplate_pkey" PRIMARY KEY (id);


--
-- TOC entry 3564 (class 2606 OID 124508)
-- Name: PlanReference PlanReference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_pkey" PRIMARY KEY (id);


--
-- TOC entry 3654 (class 2606 OID 162359)
-- Name: PlanStatus PlanStatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanStatus"
    ADD CONSTRAINT "PlanStatus_pkey" PRIMARY KEY (id);


--
-- TOC entry 3566 (class 2606 OID 124510)
-- Name: PlanUser PlanUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_pkey" PRIMARY KEY (id);


--
-- TOC entry 3658 (class 2606 OID 162383)
-- Name: PlanWorkflow PlanWorkflow_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanWorkflow"
    ADD CONSTRAINT "PlanWorkflow_pkey" PRIMARY KEY (id);


--
-- TOC entry 3558 (class 2606 OID 124512)
-- Name: Plan Plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_pkey" PRIMARY KEY (id);


--
-- TOC entry 3574 (class 2606 OID 124552)
-- Name: PrefillingSource PrefillingSource_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PrefillingSource"
    ADD CONSTRAINT "PrefillingSource_pkey" PRIMARY KEY (id);


--
-- TOC entry 3576 (class 2606 OID 124556)
-- Name: QueueInbox QueryInbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueInbox"
    ADD CONSTRAINT "QueryInbox_pkey" PRIMARY KEY (id);


--
-- TOC entry 3578 (class 2606 OID 124558)
-- Name: QueueOutbox QueueOutbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueOutbox"
    ADD CONSTRAINT "QueueOutbox_pkey" PRIMARY KEY (id);


--
-- TOC entry 3582 (class 2606 OID 124560)
-- Name: ReferenceType ReferenceType_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ReferenceType"
    ADD CONSTRAINT "ReferenceType_pkey" PRIMARY KEY (id);


--
-- TOC entry 3580 (class 2606 OID 124562)
-- Name: Reference Reference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_pkey" PRIMARY KEY (id);


--
-- TOC entry 3584 (class 2606 OID 124566)
-- Name: StorageFile StorageFile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."StorageFile"
    ADD CONSTRAINT "StorageFile_pkey" PRIMARY KEY (id);


--
-- TOC entry 3586 (class 2606 OID 124568)
-- Name: SupportiveMaterial SupportiveMaterial_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SupportiveMaterial"
    ADD CONSTRAINT "SupportiveMaterial_pkey" PRIMARY KEY (id);


--
-- TOC entry 3588 (class 2606 OID 124570)
-- Name: Tag Tag_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tag"
    ADD CONSTRAINT "Tag_pkey" PRIMARY KEY (id);


--
-- TOC entry 3592 (class 2606 OID 124572)
-- Name: TenantConfiguration TenantConfiguration_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantConfiguration"
    ADD CONSTRAINT "TenantConfiguration_pkey" PRIMARY KEY (id);


--
-- TOC entry 3594 (class 2606 OID 124574)
-- Name: TenantUser TenantUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantUser"
    ADD CONSTRAINT "TenantUser_pkey" PRIMARY KEY (id);


--
-- TOC entry 3590 (class 2606 OID 124576)
-- Name: Tenant Tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tenant"
    ADD CONSTRAINT "Tenant_pkey" PRIMARY KEY (id);


--
-- TOC entry 3652 (class 2606 OID 151348)
-- Name: UsageLimit UsageLimit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UsageLimit"
    ADD CONSTRAINT "UsageLimit_pkey" PRIMARY KEY (id);


--
-- TOC entry 3598 (class 2606 OID 124578)
-- Name: UserContactInfo UserContactInfo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserContactInfo"
    ADD CONSTRAINT "UserContactInfo_pkey" PRIMARY KEY (id);


--
-- TOC entry 3600 (class 2606 OID 124580)
-- Name: UserCredential UserCredential_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserCredential"
    ADD CONSTRAINT "UserCredential_pkey" PRIMARY KEY (id);


--
-- TOC entry 3602 (class 2606 OID 124582)
-- Name: UserDescriptionTemplate UserDatasetProfile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDatasetProfile_pkey" PRIMARY KEY (id);


--
-- TOC entry 3604 (class 2606 OID 124588)
-- Name: UserRole UserRole_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserRole"
    ADD CONSTRAINT "UserRole_pkey" PRIMARY KEY (id);


--
-- TOC entry 3596 (class 2606 OID 124592)
-- Name: User User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (id);


--
-- TOC entry 3650 (class 2606 OID 150488)
-- Name: ant_AnnotationStatus ant_AnnotationStatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_pkey" PRIMARY KEY (id);


--
-- TOC entry 3608 (class 2606 OID 124594)
-- Name: ant_Annotation ant_Annotation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_pkey" PRIMARY KEY (id);


--
-- TOC entry 3610 (class 2606 OID 124596)
-- Name: ant_EntityUser ant_EntityUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_EntityUser"
    ADD CONSTRAINT "ant_EntityUser_pkey" PRIMARY KEY (id);


--
-- TOC entry 3612 (class 2606 OID 124598)
-- Name: ant_QueueInbox ant_QueryInbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueInbox"
    ADD CONSTRAINT "ant_QueryInbox_pkey" PRIMARY KEY (id);


--
-- TOC entry 3614 (class 2606 OID 124600)
-- Name: ant_QueueOutbox ant_QueueOutbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueOutbox"
    ADD CONSTRAINT "ant_QueueOutbox_pkey" PRIMARY KEY (id);


--
-- TOC entry 3648 (class 2606 OID 150478)
-- Name: ant_Status ant_Status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Status"
    ADD CONSTRAINT "ant_Status_pkey" PRIMARY KEY (id);


--
-- TOC entry 3618 (class 2606 OID 124602)
-- Name: ant_TenantUser ant_TenantUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_TenantUser"
    ADD CONSTRAINT "ant_TenantUser_pkey" PRIMARY KEY (id);


--
-- TOC entry 3616 (class 2606 OID 124604)
-- Name: ant_Tenant ant_Tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Tenant"
    ADD CONSTRAINT "ant_Tenant_pkey" PRIMARY KEY (id);


--
-- TOC entry 3622 (class 2606 OID 124606)
-- Name: ant_UserCredential ant_UserCredential_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_UserCredential"
    ADD CONSTRAINT "ant_UserCredential_pkey" PRIMARY KEY (id);


--
-- TOC entry 3620 (class 2606 OID 124608)
-- Name: ant_User ant_User_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_User"
    ADD CONSTRAINT "ant_User_pkey" PRIMARY KEY (id);


--
-- TOC entry 3624 (class 2606 OID 124610)
-- Name: ntf_InAppNotification ntf_InAppNotification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_InAppNotification"
    ADD CONSTRAINT "ntf_InAppNotification_pkey" PRIMARY KEY (id);


--
-- TOC entry 3626 (class 2606 OID 124612)
-- Name: ntf_Notification ntf_Notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Notification"
    ADD CONSTRAINT "ntf_Notification_pkey" PRIMARY KEY (id);


--
-- TOC entry 3630 (class 2606 OID 124614)
-- Name: ntf_QueueInbox ntf_QueryInbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueInbox"
    ADD CONSTRAINT "ntf_QueryInbox_pkey" PRIMARY KEY (id);


--
-- TOC entry 3632 (class 2606 OID 124616)
-- Name: ntf_QueueOutbox ntf_QueueOutbox_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueOutbox"
    ADD CONSTRAINT "ntf_QueueOutbox_pkey" PRIMARY KEY (id);


--
-- TOC entry 3636 (class 2606 OID 124618)
-- Name: ntf_TenantConfiguration ntf_TenantConfguration_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantConfiguration"
    ADD CONSTRAINT "ntf_TenantConfguration_pkey" PRIMARY KEY (id);


--
-- TOC entry 3638 (class 2606 OID 124620)
-- Name: ntf_TenantUser ntf_TenantUser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantUser"
    ADD CONSTRAINT "ntf_TenantUser_pkey" PRIMARY KEY (id);


--
-- TOC entry 3634 (class 2606 OID 124622)
-- Name: ntf_Tenant ntf_Tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Tenant"
    ADD CONSTRAINT "ntf_Tenant_pkey" PRIMARY KEY (id);


--
-- TOC entry 3642 (class 2606 OID 124624)
-- Name: ntf_UserContactInfo ntf_UserContactInfo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserContactInfo"
    ADD CONSTRAINT "ntf_UserContactInfo_pkey" PRIMARY KEY (id);


--
-- TOC entry 3644 (class 2606 OID 124626)
-- Name: ntf_UserCredential ntf_UserCredential_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserCredential"
    ADD CONSTRAINT "ntf_UserCredential_pkey" PRIMARY KEY (id);


--
-- TOC entry 3646 (class 2606 OID 124628)
-- Name: ntf_UserNotificationPreference ntf_UserNotificationPreference_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserNotificationPreference"
    ADD CONSTRAINT "ntf_UserNotificationPreference_pkey" PRIMARY KEY (id);


--
-- TOC entry 3606 (class 2606 OID 124632)
-- Name: UserSettings user_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSettings"
    ADD CONSTRAINT user_settings_pkey PRIMARY KEY (id);


--
-- TOC entry 3661 (class 2606 OID 124635)
-- Name: ActionConfirmation ActionConfirmation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ActionConfirmation"
    ADD CONSTRAINT "ActionConfirmation_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id);


--
-- TOC entry 3662 (class 2606 OID 124640)
-- Name: ActionConfirmation ActionConfirmation_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ActionConfirmation"
    ADD CONSTRAINT "ActionConfirmation_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3668 (class 2606 OID 124740)
-- Name: DescriptionReference DescriptionReference_description_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_description_fkey" FOREIGN KEY (description_id) REFERENCES public."Description"(id);


--
-- TOC entry 3669 (class 2606 OID 124745)
-- Name: DescriptionReference DescriptionReference_reference_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_reference_fkey" FOREIGN KEY (reference_id) REFERENCES public."Reference"(id);


--
-- TOC entry 3670 (class 2606 OID 124750)
-- Name: DescriptionReference DescriptionReference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionReference"
    ADD CONSTRAINT "DescriptionReference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3746 (class 2606 OID 162372)
-- Name: DescriptionStatus DescriptionStatus_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionStatus"
    ADD CONSTRAINT "DescriptionStatus_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3671 (class 2606 OID 124755)
-- Name: DescriptionTag DescriptionTag_description_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_description_fkey" FOREIGN KEY (description) REFERENCES public."Description"(id);


--
-- TOC entry 3672 (class 2606 OID 124760)
-- Name: DescriptionTag DescriptionTag_tag_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_tag_fkey" FOREIGN KEY (tag) REFERENCES public."Tag"(id);


--
-- TOC entry 3673 (class 2606 OID 124765)
-- Name: DescriptionTag DescriptionTag_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTag"
    ADD CONSTRAINT "DescriptionTag_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3676 (class 2606 OID 124770)
-- Name: DescriptionTemplateType DescriptionTemplateType_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplateType"
    ADD CONSTRAINT "DescriptionTemplateType_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3674 (class 2606 OID 124775)
-- Name: DescriptionTemplate DescriptionTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplate"
    ADD CONSTRAINT "DescriptionTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3675 (class 2606 OID 124780)
-- Name: DescriptionTemplate DescriptionTemplate_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionTemplate"
    ADD CONSTRAINT "DescriptionTemplate_type_fkey" FOREIGN KEY (type) REFERENCES public."DescriptionTemplateType"(id);


--
-- TOC entry 3748 (class 2606 OID 162397)
-- Name: DescriptionWorkflow DescriptionWorkflow_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."DescriptionWorkflow"
    ADD CONSTRAINT "DescriptionWorkflow_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3663 (class 2606 OID 124785)
-- Name: Description Description_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id) NOT VALID;


--
-- TOC entry 3664 (class 2606 OID 124790)
-- Name: Description Description_description_template_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_description_template_fkey" FOREIGN KEY (description_template) REFERENCES public."DescriptionTemplate"(id) NOT VALID;


--
-- TOC entry 3665 (class 2606 OID 124795)
-- Name: Description Description_plan_description_template_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_plan_description_template_fkey" FOREIGN KEY (plan_description_template) REFERENCES public."PlanDescriptionTemplate"(id) NOT VALID;


--
-- TOC entry 3666 (class 2606 OID 124800)
-- Name: Description Description_plan_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_plan_fkey" FOREIGN KEY (plan) REFERENCES public."Plan"(id);


--
-- TOC entry 3667 (class 2606 OID 124805)
-- Name: Description Description_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Description"
    ADD CONSTRAINT "Description_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3689 (class 2606 OID 124870)
-- Name: EntityDoi EntityDoi_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."EntityDoi"
    ADD CONSTRAINT "EntityDoi_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3690 (class 2606 OID 124890)
-- Name: Language Language_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Language"
    ADD CONSTRAINT "Language_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3691 (class 2606 OID 124895)
-- Name: Lock Lock_lockedby_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Lock"
    ADD CONSTRAINT "Lock_lockedby_fkey" FOREIGN KEY (locked_by) REFERENCES public."User"(id);


--
-- TOC entry 3692 (class 2606 OID 124900)
-- Name: Lock Lock_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Lock"
    ADD CONSTRAINT "Lock_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3730 (class 2606 OID 124910)
-- Name: ntf_NotificationTemplate NotificationTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_NotificationTemplate"
    ADD CONSTRAINT "NotificationTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3680 (class 2606 OID 124810)
-- Name: PlanBlueprint PlanBlueprint_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanBlueprint"
    ADD CONSTRAINT "PlanBlueprint_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3681 (class 2606 OID 124815)
-- Name: PlanDescriptionTemplate PlanDescriptionTemplate_plan_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanDescriptionTemplate"
    ADD CONSTRAINT "PlanDescriptionTemplate_plan_fkey" FOREIGN KEY (plan) REFERENCES public."Plan"(id);


--
-- TOC entry 3682 (class 2606 OID 124820)
-- Name: PlanDescriptionTemplate PlanDescriptionTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanDescriptionTemplate"
    ADD CONSTRAINT "PlanDescriptionTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3683 (class 2606 OID 124825)
-- Name: PlanReference PlanReference_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_plan_id_fkey" FOREIGN KEY (plan_id) REFERENCES public."Plan"(id);


--
-- TOC entry 3684 (class 2606 OID 124830)
-- Name: PlanReference PlanReference_reference_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_reference_id_fkey" FOREIGN KEY (reference_id) REFERENCES public."Reference"(id);


--
-- TOC entry 3685 (class 2606 OID 124835)
-- Name: PlanReference PlanReference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanReference"
    ADD CONSTRAINT "PlanReference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3745 (class 2606 OID 162360)
-- Name: PlanStatus PlanStatus_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanStatus"
    ADD CONSTRAINT "PlanStatus_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3686 (class 2606 OID 124840)
-- Name: PlanUser PlanUser_plan_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_plan_fkey" FOREIGN KEY (plan) REFERENCES public."Plan"(id);


--
-- TOC entry 3687 (class 2606 OID 124845)
-- Name: PlanUser PlanUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3688 (class 2606 OID 124850)
-- Name: PlanUser PlanUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanUser"
    ADD CONSTRAINT "PlanUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id) NOT VALID;


--
-- TOC entry 3747 (class 2606 OID 162384)
-- Name: PlanWorkflow PlanWorkflow_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PlanWorkflow"
    ADD CONSTRAINT "PlanWorkflow_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3677 (class 2606 OID 124855)
-- Name: Plan Plan_blueprint_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_blueprint_fkey" FOREIGN KEY (blueprint) REFERENCES public."PlanBlueprint"(id);


--
-- TOC entry 3678 (class 2606 OID 124860)
-- Name: Plan Plan_creator_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_creator_fkey" FOREIGN KEY (creator) REFERENCES public."User"(id) NOT VALID;


--
-- TOC entry 3679 (class 2606 OID 124865)
-- Name: Plan Plan_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Plan"
    ADD CONSTRAINT "Plan_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3693 (class 2606 OID 124915)
-- Name: PrefillingSource PrefillingSource_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."PrefillingSource"
    ADD CONSTRAINT "PrefillingSource_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3694 (class 2606 OID 124925)
-- Name: QueueInbox QueryInbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueInbox"
    ADD CONSTRAINT "QueryInbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3695 (class 2606 OID 124930)
-- Name: QueueOutbox QueueOutbox_tennat_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."QueueOutbox"
    ADD CONSTRAINT "QueueOutbox_tennat_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3699 (class 2606 OID 124935)
-- Name: ReferenceType ReferenceType_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ReferenceType"
    ADD CONSTRAINT "ReferenceType_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3696 (class 2606 OID 124940)
-- Name: Reference Reference_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id) NOT VALID;


--
-- TOC entry 3697 (class 2606 OID 124945)
-- Name: Reference Reference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3698 (class 2606 OID 124950)
-- Name: Reference Reference_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Reference"
    ADD CONSTRAINT "Reference_type_fkey" FOREIGN KEY (type) REFERENCES public."ReferenceType"(id);


--
-- TOC entry 3700 (class 2606 OID 124955)
-- Name: StorageFile StorageFile_owner_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."StorageFile"
    ADD CONSTRAINT "StorageFile_owner_fkey" FOREIGN KEY (owner) REFERENCES public."User"(id);


--
-- TOC entry 3701 (class 2606 OID 124960)
-- Name: StorageFile StorageFile_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."StorageFile"
    ADD CONSTRAINT "StorageFile_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3702 (class 2606 OID 124965)
-- Name: SupportiveMaterial SupportiveMaterial_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."SupportiveMaterial"
    ADD CONSTRAINT "SupportiveMaterial_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3703 (class 2606 OID 124970)
-- Name: Tag Tag_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tag"
    ADD CONSTRAINT "Tag_created_by_fkey" FOREIGN KEY (created_by) REFERENCES public."User"(id) NOT VALID;


--
-- TOC entry 3704 (class 2606 OID 124975)
-- Name: Tag Tag_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."Tag"
    ADD CONSTRAINT "Tag_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3705 (class 2606 OID 124980)
-- Name: TenantConfiguration TenantConfiguration_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantConfiguration"
    ADD CONSTRAINT "TenantConfiguration_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3706 (class 2606 OID 124985)
-- Name: TenantUser TenantUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantUser"
    ADD CONSTRAINT "TenantUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3707 (class 2606 OID 124990)
-- Name: TenantUser TenantUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."TenantUser"
    ADD CONSTRAINT "TenantUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id);


--
-- TOC entry 3744 (class 2606 OID 151349)
-- Name: UsageLimit UsageLimit_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UsageLimit"
    ADD CONSTRAINT "UsageLimit_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id);


--
-- TOC entry 3708 (class 2606 OID 124995)
-- Name: UserContactInfo UserContactInfo_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserContactInfo"
    ADD CONSTRAINT "UserContactInfo_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id);


--
-- TOC entry 3709 (class 2606 OID 125000)
-- Name: UserCredential UserCredential_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserCredential"
    ADD CONSTRAINT "UserCredential_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id);


--
-- TOC entry 3710 (class 2606 OID 125015)
-- Name: UserDescriptionTemplate UserDatasetProfile_datasetProfile_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDatasetProfile_datasetProfile_fkey" FOREIGN KEY (description_template) REFERENCES public."DescriptionTemplate"(id);


--
-- TOC entry 3711 (class 2606 OID 125020)
-- Name: UserDescriptionTemplate UserDescriptionTemplate_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDescriptionTemplate_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3712 (class 2606 OID 125025)
-- Name: UserDescriptionTemplate UserDescriptionTemplate_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserDescriptionTemplate"
    ADD CONSTRAINT "UserDescriptionTemplate_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id) NOT VALID;


--
-- TOC entry 3713 (class 2606 OID 125030)
-- Name: UserRole UserRole_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserRole"
    ADD CONSTRAINT "UserRole_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3714 (class 2606 OID 125035)
-- Name: UserRole UserRole_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserRole"
    ADD CONSTRAINT "UserRole_user_fkey" FOREIGN KEY ("user") REFERENCES public."User"(id) NOT VALID;


--
-- TOC entry 3715 (class 2606 OID 125040)
-- Name: UserSettings UserSettings_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."UserSettings"
    ADD CONSTRAINT "UserSettings_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."Tenant"(id) NOT VALID;


--
-- TOC entry 3741 (class 2606 OID 150489)
-- Name: ant_AnnotationStatus ant_AnnotationStatus_annotation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_annotation_fkey" FOREIGN KEY (annotation_id) REFERENCES public."ant_Annotation"(id);


--
-- TOC entry 3742 (class 2606 OID 150499)
-- Name: ant_AnnotationStatus ant_AnnotationStatus_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_status_fkey" FOREIGN KEY (status_id) REFERENCES public."ant_Status"(id);


--
-- TOC entry 3743 (class 2606 OID 150494)
-- Name: ant_AnnotationStatus ant_AnnotationStatus_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_AnnotationStatus"
    ADD CONSTRAINT "ant_AnnotationStatus_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- TOC entry 3716 (class 2606 OID 125050)
-- Name: ant_Annotation ant_Annotation_parent_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_parent_fkey" FOREIGN KEY (parent_id) REFERENCES public."ant_Annotation"(id);


--
-- TOC entry 3717 (class 2606 OID 125055)
-- Name: ant_Annotation ant_Annotation_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- TOC entry 3718 (class 2606 OID 125060)
-- Name: ant_Annotation ant_Annotation_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Annotation"
    ADD CONSTRAINT "ant_Annotation_user_fkey" FOREIGN KEY (subject_id) REFERENCES public."ant_User"(id);


--
-- TOC entry 3719 (class 2606 OID 125065)
-- Name: ant_EntityUser ant_EntityUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_EntityUser"
    ADD CONSTRAINT "ant_EntityUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- TOC entry 3720 (class 2606 OID 125070)
-- Name: ant_EntityUser ant_EntityUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_EntityUser"
    ADD CONSTRAINT "ant_EntityUser_user_fkey" FOREIGN KEY (user_id) REFERENCES public."ant_User"(id);


--
-- TOC entry 3721 (class 2606 OID 125075)
-- Name: ant_QueueInbox ant_QueryInbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueInbox"
    ADD CONSTRAINT "ant_QueryInbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- TOC entry 3722 (class 2606 OID 125080)
-- Name: ant_QueueOutbox ant_QueueOutbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_QueueOutbox"
    ADD CONSTRAINT "ant_QueueOutbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- TOC entry 3740 (class 2606 OID 150479)
-- Name: ant_Status ant_Status_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_Status"
    ADD CONSTRAINT "ant_Status_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- TOC entry 3723 (class 2606 OID 125085)
-- Name: ant_TenantUser ant_TenantUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_TenantUser"
    ADD CONSTRAINT "ant_TenantUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ant_Tenant"(id);


--
-- TOC entry 3724 (class 2606 OID 125090)
-- Name: ant_TenantUser ant_TenantUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_TenantUser"
    ADD CONSTRAINT "ant_TenantUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."ant_User"(id);


--
-- TOC entry 3725 (class 2606 OID 125095)
-- Name: ant_UserCredential ant_UserCredential_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ant_UserCredential"
    ADD CONSTRAINT "ant_UserCredential_user_fkey" FOREIGN KEY ("user") REFERENCES public."ant_User"(id);


--
-- TOC entry 3726 (class 2606 OID 125135)
-- Name: ntf_InAppNotification ntf_InAppNotification_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_InAppNotification"
    ADD CONSTRAINT "ntf_InAppNotification_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3727 (class 2606 OID 125140)
-- Name: ntf_InAppNotification ntf_InAppNotification_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_InAppNotification"
    ADD CONSTRAINT "ntf_InAppNotification_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- TOC entry 3728 (class 2606 OID 125145)
-- Name: ntf_Notification ntf_Notification_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Notification"
    ADD CONSTRAINT "ntf_Notification_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3729 (class 2606 OID 125150)
-- Name: ntf_Notification ntf_Notification_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_Notification"
    ADD CONSTRAINT "ntf_Notification_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- TOC entry 3731 (class 2606 OID 125155)
-- Name: ntf_QueueInbox ntf_QueryInbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueInbox"
    ADD CONSTRAINT "ntf_QueryInbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3732 (class 2606 OID 125160)
-- Name: ntf_QueueOutbox ntf_QueueOutbox_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_QueueOutbox"
    ADD CONSTRAINT "ntf_QueueOutbox_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3733 (class 2606 OID 125165)
-- Name: ntf_TenantConfiguration ntf_TenantConfiguration_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantConfiguration"
    ADD CONSTRAINT "ntf_TenantConfiguration_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3734 (class 2606 OID 125170)
-- Name: ntf_TenantUser ntf_TenantUser_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantUser"
    ADD CONSTRAINT "ntf_TenantUser_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3735 (class 2606 OID 125175)
-- Name: ntf_TenantUser ntf_TenantUser_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_TenantUser"
    ADD CONSTRAINT "ntf_TenantUser_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- TOC entry 3736 (class 2606 OID 125180)
-- Name: ntf_UserContactInfo ntf_UserContactInfo_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserContactInfo"
    ADD CONSTRAINT "ntf_UserContactInfo_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- TOC entry 3737 (class 2606 OID 125185)
-- Name: ntf_UserCredential ntf_UserCredential_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserCredential"
    ADD CONSTRAINT "ntf_UserCredential_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


--
-- TOC entry 3738 (class 2606 OID 125190)
-- Name: ntf_UserNotificationPreference ntf_UserNotificationPreference_tenant_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserNotificationPreference"
    ADD CONSTRAINT "ntf_UserNotificationPreference_tenant_fkey" FOREIGN KEY (tenant) REFERENCES public."ntf_Tenant"(id);


--
-- TOC entry 3739 (class 2606 OID 125195)
-- Name: ntf_UserNotificationPreference ntf_UserNotificationPreference_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."ntf_UserNotificationPreference"
    ADD CONSTRAINT "ntf_UserNotificationPreference_user_fkey" FOREIGN KEY ("user") REFERENCES public."ntf_User"(id);


-- Completed on 2024-10-16 10:21:54

--
-- PostgreSQL database dump complete
--

