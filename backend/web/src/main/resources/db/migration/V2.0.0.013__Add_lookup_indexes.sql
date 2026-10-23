-- The columns the queries and builders filter and join on; until now these tables had only their primary keys.
CREATE INDEX IF NOT EXISTS "PlanReference_plan_id_idx" ON public."PlanReference" (plan_id);
CREATE INDEX IF NOT EXISTS "PlanReference_reference_id_idx" ON public."PlanReference" (reference_id);

CREATE INDEX IF NOT EXISTS "DescriptionReference_description_id_idx" ON public."DescriptionReference" (description_id);
CREATE INDEX IF NOT EXISTS "DescriptionReference_reference_id_idx" ON public."DescriptionReference" (reference_id);

CREATE INDEX IF NOT EXISTS "PlanUser_plan_idx" ON public."PlanUser" (plan);
CREATE INDEX IF NOT EXISTS "PlanUser_user_idx" ON public."PlanUser" ("user");

CREATE INDEX IF NOT EXISTS "Description_plan_idx" ON public."Description" (plan);
CREATE INDEX IF NOT EXISTS "Description_plan_description_template_idx" ON public."Description" (plan_description_template);
CREATE INDEX IF NOT EXISTS "Description_description_template_idx" ON public."Description" (description_template);

CREATE INDEX IF NOT EXISTS "PlanDescriptionTemplate_plan_idx" ON public."PlanDescriptionTemplate" (plan);
CREATE INDEX IF NOT EXISTS "PlanDescriptionTemplate_description_template_group_idx" ON public."PlanDescriptionTemplate" (description_template_group);

CREATE INDEX IF NOT EXISTS "DescriptionTag_description_idx" ON public."DescriptionTag" (description);
CREATE INDEX IF NOT EXISTS "DescriptionTag_tag_idx" ON public."DescriptionTag" (tag);

CREATE INDEX IF NOT EXISTS "Plan_group_id_idx" ON public."Plan" (group_id);
CREATE INDEX IF NOT EXISTS "Plan_blueprint_idx" ON public."Plan" (blueprint);

CREATE INDEX IF NOT EXISTS "Reference_type_idx" ON public."Reference" (type);
CREATE INDEX IF NOT EXISTS "Reference_reference_idx" ON public."Reference" (reference);

CREATE INDEX IF NOT EXISTS "UserContactInfo_user_idx" ON public."UserContactInfo" ("user");
CREATE INDEX IF NOT EXISTS "UserContactInfo_value_idx" ON public."UserContactInfo" (value);

CREATE INDEX IF NOT EXISTS "UserCredential_user_idx" ON public."UserCredential" ("user");
CREATE INDEX IF NOT EXISTS "UserCredential_external_id_idx" ON public."UserCredential" (external_id);

CREATE INDEX IF NOT EXISTS "UserRole_user_idx" ON public."UserRole" ("user");

CREATE INDEX IF NOT EXISTS "EntityDoi_entity_id_idx" ON public."EntityDoi" (entity_id);

CREATE INDEX IF NOT EXISTS "Evaluation_entity_idx" ON public."Evaluation" (entity);

CREATE INDEX IF NOT EXISTS "Lock_target_idx" ON public."Lock" (target);
