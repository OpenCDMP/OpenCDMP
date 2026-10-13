import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { StructureEditorBase } from '../../structure-editor-base';
import { IconPickerComponent } from '../icon-picker/icon-picker.component';
import { MatFormField, MatLabel, MatError, MatSuffix, MatHint } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { SingleAutoCompleteComponent } from '../../../../../library/auto-complete/single/single-auto-complete.component';
import { FormFocusDirective } from '../../../../../../common/forms/form-focus.directive';
import { RichTextEditorComponent } from '../../../../../library/rich-text-editor/rich-text-editor.component';
import { TranslatePipe } from '@ngx-translate/core';

/**
 * The About card both editors open with: icon, name, code, type and description, editable or read-only. An
 * editor adds its own fields under the type with `se2AboutFields`, and their read-only rows with `se2AboutRead`.
 */
@Component({
    selector: 'app-se2-about-card',
    templateUrl: './about-card.component.html',
    styleUrls: ['./about-card.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [IconPickerComponent, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatError, MatIcon, MatSuffix, MatTooltip, MatHint, SingleAutoCompleteComponent, FormFocusDirective, RichTextEditorComponent, TranslatePipe]
})
export class AboutCardComponent {
	@Input() typeConfiguration: SingleAutoCompleteConfiguration;
	@Input() typeName: string;
	@Input() typeRequired = false;
	@Input() descriptionRequired = false;

	constructor(public editor: StructureEditorBase<any, any>) { }

	get form(): UntypedFormGroup { return this.editor.formGroup as unknown as UntypedFormGroup; }
	get iconForm(): UntypedFormGroup { return this.form.get('definition').get('iconConfiguration') as UntypedFormGroup; }
	get codeLocked(): boolean { return this.form.get('code').disabled; }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }
	t(key: string): string { return this.editor.term(key); }
}
