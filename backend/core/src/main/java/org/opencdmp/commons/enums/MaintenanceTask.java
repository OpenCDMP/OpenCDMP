package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

// Declaration order is the order a run executes its tasks in: the index first, then touch, accounting and KPI events.
public enum MaintenanceTask implements DatabaseEnum<String> {
	ElasticIndex("elastic-index"),

	TenantTouch("tenant-touch"),
	UserTouch("user-touch"),
	DescriptionTouch("description-touch"),
	PlanTouch("plan-touch"),

	PlanAccounting("plan-accounting"),
	DescriptionAccounting("description-accounting"),
	PlanBlueprintAccounting("plan-blueprint-accounting"),
	PlanBlueprintTypeAccounting("plan-blueprint-type-accounting"),
	DescriptionTemplateAccounting("description-template-accounting"),
	DescriptionTemplateTypeAccounting("description-template-type-accounting"),
	PrefillingSourceAccounting("prefilling-source-accounting"),
	ReferenceAccounting("reference-accounting"),
	ReferenceTypeAccounting("reference-type-accounting"),
	PlanStatusAccounting("plan-status-accounting"),
	DescriptionStatusAccounting("description-status-accounting"),
	LanguageAccounting("language-accounting"),
	UserAccounting("user-accounting"),
	PlanEvaluationAccounting("plan-evaluation-accounting"),
	DescriptionEvaluationAccounting("description-evaluation-accounting"),

	IndicatorCreate("indicator-create"),
	IndicatorReset("indicator-reset"),
	IndicatorAccess("indicator-access"),
	DashboardTouch("dashboard-touch"),
	IndicatorPointPlan("indicator-point-plan"),
	IndicatorPointDescription("indicator-point-description"),
	IndicatorPointReference("indicator-point-reference"),
	IndicatorPointUser("indicator-point-user"),
	IndicatorPointPlanBlueprint("indicator-point-plan-blueprint"),
	IndicatorPointDescriptionTemplate("indicator-point-description-template"),
	IndicatorPointTenant("indicator-point-tenant"),
	IndicatorPointPlanBlueprintType("indicator-point-plan-blueprint-type"),
	IndicatorPointDescriptionTemplateType("indicator-point-description-template-type");

	private final String value;

	MaintenanceTask(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	private static final Map<String, MaintenanceTask> map = EnumUtils.getEnumValueMap(MaintenanceTask.class);

	public static MaintenanceTask of(String i) {
		return map.get(i);
	}
}
