package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum UsageLimitTargetMetric implements DatabaseEnum<String> {
	USER_COUNT(TargetMetrics.UserCount),
	PLAN_COUNT(TargetMetrics.PlanCount),
	PLAN_STATUS_COUNT(TargetMetrics.PlanStatusCount),
	BLUEPRINT_COUNT(TargetMetrics.BlueprintCount),
	DESCRIPTION_COUNT(TargetMetrics.DescriptionCount),
	DESCRIPTION_STATUS_COUNT(TargetMetrics.DescriptionStatusCount),
	DESCRIPTION_TEMPLATE_COUNT(TargetMetrics.DescriptionTemplateCount),
	DESCRIPTION_TEMPLATE_TYPE_COUNT(TargetMetrics.DescriptionTemplateTypeCount),
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
	IMPORT_DESCRIPTION_TEMPLATE_XML_EXECUTION_COUNT(TargetMetrics.ImportDescriptionTemplateXMLExecutionCount);
	private final String value;

	public static class TargetMetrics {
		public static final String UserCount = "user_count";
		public static final String PlanCount = "plan_count";
		public static final String PlanStatusCount = "plan_status_count";
		public static final String BlueprintCount = "blueprint_count";
		public static final String DescriptionCount = "description_count";
		public static final String DescriptionStatusCount = "description_status_count";
		public static final String DescriptionTemplateCount = "description_template_count";
		public static final String DescriptionTemplateTypeCount = "description_template_type_count";
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
