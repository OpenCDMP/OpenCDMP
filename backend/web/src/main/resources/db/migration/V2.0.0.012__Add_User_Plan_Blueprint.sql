CREATE TABLE IF NOT EXISTS public."UserPlanBlueprint"
(
    id uuid NOT NULL,
    "user" uuid NOT NULL,
    plan_blueprint uuid NOT NULL,
    role smallint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    is_active smallint NOT NULL,
    tenant uuid,
    CONSTRAINT "UserPlanBlueprint_pkey" PRIMARY KEY (id),
    CONSTRAINT "UserPlanBlueprint_user_fkey" FOREIGN KEY ("user")
        REFERENCES public."User" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT "UserPlanBlueprint_plan_blueprint_fkey" FOREIGN KEY (plan_blueprint)
        REFERENCES public."PlanBlueprint" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT "UserPlanBlueprint_tenant_fkey" FOREIGN KEY (tenant)
        REFERENCES public."Tenant" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS "UserPlanBlueprint_plan_blueprint_idx" ON public."UserPlanBlueprint" (plan_blueprint);
CREATE INDEX IF NOT EXISTS "UserPlanBlueprint_user_idx" ON public."UserPlanBlueprint" ("user");
