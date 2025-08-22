import { EvaluatorEntityType } from "@app/core/common/enum/evaluator-entity-type";
import { RankType } from "./rank-type";
import { SelectionConfiguration } from "./evaluator-selection";
import { ValueRangeConfiguration } from "./evaluator-value-range";
import { ConfigurationField } from "../plugin-configuration/plugin-configuration";
import { PluginType } from "@app/core/common/enum/plugin-type";

export interface EvaluatorFormat {
	rankType: RankType[];
	selectionConfiguration: SelectionConfiguration;
	valueRangeConfiguration: ValueRangeConfiguration;
	evaluatorId: string;
	entityTypes: EvaluatorEntityType[];
	configurationFields?: ConfigurationField[];
	userConfigurationFields?: ConfigurationField[];
	pluginType?: PluginType;
}