import { Injectable } from "@angular/core";
import { PlanWorkflow, PlanWorkflowDefinition, PlanWorkflowDefinitionTransition } from "@app/core/model/workflow/plan-workflow";
import { PlanWorkflowService } from "@app/core/services/plan/plan-workflow.service";
import { BaseEditorResolver } from "@common/base/base-editor.resolver";
import { takeUntil } from "rxjs";
import { nameof } from "ts-simple-nameof";

@Injectable()
export class PlanWorkflowEditorResolver extends BaseEditorResolver {

	constructor(private planWorkflowService: PlanWorkflowService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<PlanWorkflow>(x => x.name),
			nameof<PlanWorkflow>(x => x.description),
			nameof<PlanWorkflow>(x => x.definition),

			[nameof<PlanWorkflow>(x => x.definition), nameof<PlanWorkflowDefinition>(x => x.startingStatus.id)].join('.'),
			[nameof<PlanWorkflow>(x => x.definition), nameof<PlanWorkflowDefinition>(x => x.startingStatus.name)].join('.'),
			[nameof<PlanWorkflow>(x => x.definition), nameof<PlanWorkflowDefinition>(x => x.statusTransitions)].join('.'),
			[nameof<PlanWorkflow>(x => x.definition), nameof<PlanWorkflowDefinition>(x => x.statusTransitions), nameof<PlanWorkflowDefinitionTransition>(x => x.fromStatus.id)].join('.'),
			[nameof<PlanWorkflow>(x => x.definition), nameof<PlanWorkflowDefinition>(x => x.statusTransitions), nameof<PlanWorkflowDefinitionTransition>(x => x.fromStatus.name)].join('.'),
			[nameof<PlanWorkflow>(x => x.definition), nameof<PlanWorkflowDefinition>(x => x.statusTransitions), nameof<PlanWorkflowDefinitionTransition>(x => x.toStatus.id)].join('.'),
			[nameof<PlanWorkflow>(x => x.definition), nameof<PlanWorkflowDefinition>(x => x.statusTransitions), nameof<PlanWorkflowDefinitionTransition>(x => x.toStatus.name)].join('.'),
		]
	}

	resolve() {

		const fields = [
			...PlanWorkflowEditorResolver.lookupFields()
		];

		return this.planWorkflowService.getCurrent(fields).pipe(takeUntil(this._destroyed));
	}
}
