import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { KnownObject, objectsOf } from '../../semantic-objects';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { Bp2SemanticsPickerComponent } from '../semantics-picker/semantics-picker.component';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

interface ObjectLine { key: string; known: KnownObject; parentLabel: string; }

/**
 * "This section is …" / "This question is …": the plugin objects a section or question describes. A section makes
 * one object for everything inside it; a question makes one object per answer. Free text is allowed, like keys.
 */
@Component({
    selector: 'app-se2-object-field',
    templateUrl: './object-field.component.html',
    styleUrls: ['./object-field.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, Bp2SemanticsPickerComponent, MatButton, TranslatePipe]
})
export class ObjectFieldComponent {
	@Input() form: UntypedFormGroup;
	@Input() kind: 'section' | 'question' = 'question';
	@Input() label: string;
	/** Asks the question to allow more than one answer, for an object the output holds a list of. */
	@Output() allowMore = new EventEmitter<void>();

	constructor(public editor: StructureEditorBase<any, any>) { }

	get objects(): string[] { return objectsOf(this.form); }
	get repeatable(): boolean { return !!this.form?.get('hasMultiplicity')?.value; }

	get lines(): ObjectLine[] {
		const known = this.editor.knownObjects();
		return this.objects.map(key => {
			const object = known.find(o => o.key === key) ?? null;
			const parent = object?.parent ? known.find(o => o.key === object.parent) : null;
			return { key, known: object, parentLabel: parent?.label ?? object?.parent ?? null };
		});
	}

	/** A question describing a list object gives one per answer, so more than one answer should be allowed. */
	suggestRepeat(line: ObjectLine): boolean { return this.kind === 'question' && !!line.known?.multiple && !this.repeatable && !this.editor.readOnly; }
}
