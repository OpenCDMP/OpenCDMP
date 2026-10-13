import { Injectable, signal } from '@angular/core';
import { Guid } from '@common/types/guid';

/**
 * Component-scoped service that exposes the description id currently being
 * rendered by the description-form chain. Provided by PlanDescriptionEditorComponent
 * so that descendant components (e.g. form-field) can read it via hierarchical DI
 * without threading the id through every Input.
 */
@Injectable()
export class DescriptionContextService {
	private readonly _descriptionId = signal<Guid | null>(null);
	private readonly _descriptionLabel = signal<string | null>(null);

	readonly descriptionId = this._descriptionId.asReadonly();
	readonly descriptionLabel = this._descriptionLabel.asReadonly();

	setDescriptionId(id: Guid | null): void {
		this._descriptionId.set(id);
	}

	setDescriptionLabel(label: string | null): void {
		this._descriptionLabel.set(label);
	}
}
