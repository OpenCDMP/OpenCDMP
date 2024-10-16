import { Injectable } from "@angular/core";
import { DescriptionWorkflow, DescriptionWorkflowDefinition, DescriptionWorkflowDefinitionTransition } from "@app/core/model/workflow/description-workflow";
import { DescriptionWorkflowService } from "@app/core/services/description-workflow/description-workflow.service";
import { BaseEditorResolver } from "@common/base/base-editor.resolver";
import { takeUntil } from "rxjs";
import { nameof } from "ts-simple-nameof";

@Injectable()
export class DescriptionWorkflowEditorResolver extends BaseEditorResolver {

	constructor(private descriptionWorkflowService: DescriptionWorkflowService) {
		super();
	}

	public static lookupFields(): string[] {
		return [
			...BaseEditorResolver.lookupFields(),
			nameof<DescriptionWorkflow>(x => x.name),
			nameof<DescriptionWorkflow>(x => x.description),
			nameof<DescriptionWorkflow>(x => x.definition),

			[nameof<DescriptionWorkflow>(x => x.definition), nameof<DescriptionWorkflowDefinition>(x => x.startingStatus.id)].join('.'),
			[nameof<DescriptionWorkflow>(x => x.definition), nameof<DescriptionWorkflowDefinition>(x => x.startingStatus.name)].join('.'),
			[nameof<DescriptionWorkflow>(x => x.definition), nameof<DescriptionWorkflowDefinition>(x => x.statusTransitions)].join('.'),
			[nameof<DescriptionWorkflow>(x => x.definition), nameof<DescriptionWorkflowDefinition>(x => x.statusTransitions), nameof<DescriptionWorkflowDefinitionTransition>(x => x.fromStatus.id)].join('.'),
			[nameof<DescriptionWorkflow>(x => x.definition), nameof<DescriptionWorkflowDefinition>(x => x.statusTransitions), nameof<DescriptionWorkflowDefinitionTransition>(x => x.fromStatus.name)].join('.'),
			[nameof<DescriptionWorkflow>(x => x.definition), nameof<DescriptionWorkflowDefinition>(x => x.statusTransitions), nameof<DescriptionWorkflowDefinitionTransition>(x => x.toStatus.id)].join('.'),
			[nameof<DescriptionWorkflow>(x => x.definition), nameof<DescriptionWorkflowDefinition>(x => x.statusTransitions), nameof<DescriptionWorkflowDefinitionTransition>(x => x.toStatus.name)].join('.'),
		]
	}

	resolve() {

		const fields = [
			...DescriptionWorkflowEditorResolver.lookupFields()
		];

		return this.descriptionWorkflowService.getCurrent(fields).pipe(takeUntil(this._destroyed));
	}
}