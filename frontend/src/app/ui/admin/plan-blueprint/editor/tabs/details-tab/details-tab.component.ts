import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PlanBlueprintEditorComponent } from '../../plan-blueprint-editor.component';
import { AboutCardComponent } from '../../../../structure-editor/details/about-card/about-card.component';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatFormField, MatLabel, MatHint, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormFocusDirective } from '../../../../../../../common/forms/form-focus.directive';
import { RichTextEditorComponent } from '../../../../../../library/rich-text-editor/rich-text-editor.component';
import { ExportCardComponent } from '../../../../structure-editor/details/export-card/export-card.component';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

/**
 * Details tab: what the blueprint is, how authors see it, and what is left before it can be finalized. The About,
 * export and readiness cards are shared with the description template editor; consent and the preview are the blueprint's.
 */
@Component({
    selector: 'app-bp2-details-tab',
    templateUrl: './details-tab.component.html',
    styleUrls: ['./details-tab.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AboutCardComponent, MatSlideToggle, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatHint, MatError, FormFocusDirective, RichTextEditorComponent, ExportCardComponent, MatIcon, TranslatePipe]
})
export class Bp2DetailsTabComponent {
	constructor(public editor: PlanBlueprintEditorComponent) { }

	get form() { return this.editor.formGroup; }
	get definition(): UntypedFormGroup { return this.form.controls.definition as UntypedFormGroup; }
	get consent(): UntypedFormGroup { return this.definition.get('planCreationConsent') as UntypedFormGroup; }
	get consentEnabled(): boolean { return !!this.consent.get('enabled').value; }
	get iconForm(): UntypedFormGroup { return this.definition.get('iconConfiguration') as UntypedFormGroup; }
	get hasPlugins(): boolean { return (this.editor.plugins?.length ?? 0) > 0; }

	descriptionSnippet(): string {
		const html: string = this.form.get('description').value ?? '';
		const text = html.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
		return text.length > 180 ? text.slice(0, 180) + '…' : text;
	}
}
