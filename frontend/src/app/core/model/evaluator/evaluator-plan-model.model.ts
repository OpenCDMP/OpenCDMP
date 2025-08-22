export class RankResultModel {
    rank: number;
    details: string;
    results: EvaluationResultModel[];
}

export class EvaluationResultModel {
    rank: number;
    benchmarkTitle: string;
    benchmarkDetails: string;
    metrics: EvaluationResultMetricModel[];
}

export class EvaluationResultMetricModel {
    rank: number;
    metricTitle: string;
    metricDetails: string;
    messages: EvaluationResultMessageModel[];
}


export class EvaluationResultMessageModel {
    title?: string;
    message: string;
}