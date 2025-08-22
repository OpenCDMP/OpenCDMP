import { RankType } from "./rank-type";
import { EvaluatorEntityType } from "@app/core/common/enum/evaluator-entity-type";
import { RankConfig } from "./rank-config";
import { PluginType } from "@app/core/common/enum/plugin-type";
import { ConfigurationField } from "../plugin-configuration/plugin-configuration";
import { PluginEntityType } from "@app/core/common/enum/plugin-entity-type";

export class EvaluatorConfiguration{
    evaluatorId: string;
    rankType: RankType[];
    evaluatorEntityTypes: EvaluatorEntityType[];
    rankConfig: RankConfig[];
    hasLogo: boolean;
    configurationFields?: ConfigurationField[];
    userConfigurationFields?: ConfigurationField[];
    availableBenchmarks: BenchmarkConfiguration[];
    pluginType?: PluginType;
}

export class BenchmarkConfiguration{
    id: string;
    label: string;
    appliesTo: PluginEntityType[];
}