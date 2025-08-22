package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum UsageLimitTargetMetric implements DatabaseEnum<String> {
	USER_COUNT(TargetMetrics.UserCount),
	PLAN_COUNT(TargetMetrics.PlanCount),
	PLAN_BY_STATUS_COUNT(TargetMetrics.PlanByStatusCount),
	PLAN_PUBLISHED_COUNT(TargetMetrics.PlanPublishedCount),
	PLAN_DOIED_COUNT(TargetMetrics.PlanDoiedCount),
	PLAN_STATUS_COUNT(TargetMetrics.PlanStatusCount),
	BLUEPRINT_COUNT(TargetMetrics.BlueprintCount),
	BLUEPRINT_DRAFT_COUNT(TargetMetrics.BlueprintDraftCount),
	BLUEPRINT_FINALIZED_COUNT(TargetMetrics.BlueprintFinalizedCount),
	DESCRIPTION_COUNT(TargetMetrics.DescriptionCount),
	DESCRIPTION_TEMPLATE_USED_COUNT(TargetMetrics.DescriptionTemplateUsedCount),
	DESCRIPTION_PUBLISHED_COUNT(TargetMetrics.DescriptionPublishedCount),
	DESCRIPTION_DOIED_COUNT(TargetMetrics.DescriptionDoiedCount),
	DESCRIPTION_BY_STATUS_COUNT(TargetMetrics.DescriptionByStatusCount),
	DESCRIPTION_STATUS_COUNT(TargetMetrics.DescriptionStatusCount),
	DESCRIPTION_TEMPLATE_COUNT(TargetMetrics.DescriptionTemplateCount),
	DESCRIPTION_TEMPLATE_DRAFT_COUNT(TargetMetrics.DescriptionTemplateDraftCount),
	DESCRIPTION_TEMPLATE_FINALIZED_COUNT(TargetMetrics.DescriptionTemplateFinalizedCount),
	DESCRIPTION_TEMPLATE_TYPE_COUNT(TargetMetrics.DescriptionTemplateTypeCount),
	REFERENCE_COUNT(TargetMetrics.ReferenceCount),
	REFERENCE_BY_TYPE_COUNT(TargetMetrics.ReferenceByTypeCount),
	PREFILLING_SOURCES_COUNT(TargetMetrics.PrefillingSourcesCount),
	EVALUATION_PLAN_EXECUTION_COUNT(TargetMetrics.EvaluationPlanExecutionCount),
	EVALUATION_PLAN_EXECUTION_COUNT_FOR(TargetMetrics.EvaluationPlanExecutionCountFor),
	EVALUATION_DESCRIPTION_EXECUTION_COUNT(TargetMetrics.EvaluationDescriptionExecutionCount),
	EVALUATION_DESCRIPTION_EXECUTION_COUNT_FOR(TargetMetrics.EvaluationDescriptionExecutionCountFor),
	REFERENCE_TYPE_COUNT(TargetMetrics.ReferenceTypeCount),
	DEPOSIT_EXECUTION_COUNT(TargetMetrics.DepositExecutionCount),
	DEPOSIT_EXECUTION_COUNT_FOR(TargetMetrics.DepositExecutionCountFor_),
	FILE_TRANSFORMER_EXPORT_PLAN_EXECUTION_COUNT(TargetMetrics.FileTransformerExportPlanExecutionCount),
	FILE_TRANSFORMER_EXPORT_PLAN_EXECUTION_COUNT_FOR(TargetMetrics.FileTransformerExportPlanExecutionCountFor_),
	FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT(TargetMetrics.FileTransformerExportDescriptionExecutionCount),
	FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT_FOR(TargetMetrics.FileTransformerExportDescriptionExecutionCountFor_),
	FILE_TRANSFORMER_IMPORT_PLAN_EXECUTION_COUNT(TargetMetrics.FileTransformerImportPlanExecutionCount),
	FILE_TRANSFORMER_IMPORT_PLAN_EXECUTION_COUNT_FOR(TargetMetrics.FileTransformerImportPlanExecutionCountFor_),
	EXPORT_PLAN_XML_EXECUTION_COUNT(TargetMetrics.ExportPlanXMLExecutionCount),
	EXPORT_DESCRIPTION_XML_EXECUTION_COUNT(TargetMetrics.ExportDescriptionXMLExecutionCount),
	EXPORT_BLUEPRINT_XML_EXECUTION_COUNT(TargetMetrics.ExportBlueprintXMLExecutionCount),
	EXPORT_DESCRIPTION_TEMPLATE_XML_EXECUTION_COUNT(TargetMetrics.ExportDescriptionTemplateXMLExecutionCount),
	IMPORT_PLAN_XML_EXECUTION_COUNT(TargetMetrics.ImportPlanXMLExecutionCount),
	IMPORT_BLUEPRINT_XML_EXECUTION_COUNT(TargetMetrics.ImportBlueprintXMLExecutionCount),
	IMPORT_DESCRIPTION_TEMPLATE_XML_EXECUTION_COUNT(TargetMetrics.ImportDescriptionTemplateXMLExecutionCount),
	LANGUAGE_COUNT(TargetMetrics.LanguageCount);
	private final String value;

	public static class TargetMetrics {
		public static final String UserCount = "user_count";
		public static final String PlanCount = "plan_count";
		public static final String PlanByStatusCount = "plan_by_{status_name}_count";
		public static final String PlanStatusCount = "plan_status_count";
		public static final String PlanPublishedCount = "plan_published_count";
		public static final String PlanDoiedCount = "plan_doied_count";
		public static final String BlueprintCount = "blueprint_count";
		public static final String BlueprintDraftCount = "plan_blueprint_draft_count";
		public static final String BlueprintFinalizedCount = "plan_blueprint_finalized_count";
		public static final String DescriptionCount = "description_count";
		public static final String DescriptionPublishedCount = "description_published_count";
		public static final String DescriptionDoiedCount = "description_doied_count";
		public static final String DescriptionByStatusCount = "description_by_{status_name}_count";
		public static final String DescriptionStatusCount = "description_status_count";
		public static final String DescriptionTemplateCount = "description_template_count";
		public static final String DescriptionTemplateUsedCount = "description_template_used_count";
		public static final String DescriptionTemplateDraftCount = "description_template_draft_count";
		public static final String DescriptionTemplateFinalizedCount = "description_template_finalized_count";
		public static final String DescriptionTemplateTypeCount = "description_template_type_count";
		public static final String ReferenceCount = "reference_count";
		public static final String ReferenceByTypeCount = "reference_by_{type_code}_count";
		public static final String LanguageCount = "language_count";
		public static final String PrefillingSourcesCount = "prefilling_sources_count";
		public static final String EvaluationPlanExecutionCount = "evaluation_plan_execution_count";
		public static final String EvaluationPlanExecutionCountFor = "evaluation_plan_execution_count_for_";
		public static final String EvaluationDescriptionExecutionCount = "evaluation_description_execution_count";
		public static final String EvaluationDescriptionExecutionCountFor = "evaluation_description_execution_count_for_";
		public static final String ReferenceTypeCount = "reference_type_count";
		public static final String DepositExecutionCount = "deposit_execution_count";
		public static final String DepositExecutionCountFor_ = "deposit_execution_count_for_";
		public static final String FileTransformerExportPlanExecutionCount = "file_transformer_export_plan_execution_count";
		public static final String FileTransformerExportPlanExecutionCountFor_ = "file_transformer_export_plan_execution_count_for_";
		public static final String FileTransformerExportDescriptionExecutionCount = "file_transformer_export_description_execution_count";
		public static final String FileTransformerExportDescriptionExecutionCountFor_ = "file_transformer_export_description_execution_count_for_";
		public static final String FileTransformerImportPlanExecutionCount = "file_transformer_import_plan_execution_count";
		public static final String FileTransformerImportPlanExecutionCountFor_ = "file_transformer_import_plan_execution_count_for_";
		public static final String ExportPlanXMLExecutionCount = "export_plan_xml_execution_count";
		public static final String ExportDescriptionXMLExecutionCount = "export_description_xml_execution_count";
		public static final String ExportBlueprintXMLExecutionCount = "export_blueprint_xml_execution_count";
		public static final String ExportDescriptionTemplateXMLExecutionCount = "export_description_template_xml_execution_count";
		public static final String ImportPlanXMLExecutionCount = "import_plan_xml_execution_count";
		public static final String ImportBlueprintXMLExecutionCount = "import_blueprint_xml_execution_count";
		public static final String ImportDescriptionTemplateXMLExecutionCount = "import_description_template_xml_execution_count";
	}

	UsageLimitTargetMetric(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return this.value;
	}

	private static final Map<String, UsageLimitTargetMetric> map = EnumUtils.getEnumValueMap(UsageLimitTargetMetric.class);

	public static UsageLimitTargetMetric of(String i) {
		return map.get(i);
	}
}
