export enum SuccessStatus {
	Fail = "Fail",
	Pass = "Pass",
	/**
	 * The target was not assessed at all, so neither Pass nor Fail applies. The evaluator
	 * puts the reason in the rank result details.
	 */
	NotEvaluated = "NotEvaluated"
}
