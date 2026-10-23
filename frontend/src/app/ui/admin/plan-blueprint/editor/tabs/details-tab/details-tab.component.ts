import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatButton } from '@angular/material/button';
import { UserPlanBlueprintRole } from '@app/core/common/enum/user-plan-blueprint-role';
import { EditorsPanelComponent } from '../../editors-panel/editors-panel.component';
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
import { Bp2CountPipe } from '../../../../structure-editor/count.pipe';

/**
 * Details tab: what the blueprint is, who may change it, how authors see it, and what is left before it can be finalized. The About,
 * export and readiness cards are shared with the description template editor; consent and the preview are the blueprint's.
 */
@Component({
    selector: 'app-bp2-details-tab',
    templateUrl: './details-tab.component.html',
    styleUrls: ['./details-tab.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AboutCardComponent, MatSlideToggle, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatHint, MatError, FormFocusDirective, RichTextEditorComponent, ExportCardComponent, MatIcon, MatButton, TranslatePipe, Bp2CountPipe]
})
export class Bp2DetailsTabComponent {
	constructor(public editor: PlanBlueprintEditorComponent, private dialog: MatDialog) { }

	get form() { return this.editor.formGroup; }
	get definition(): UntypedFormGroup { return this.form.controls.definition as UntypedFormGroup; }
	get consent(): UntypedFormGroup { return this.definition.get('planCreationConsent') as UntypedFormGroup; }
	get consentEnabled(): boolean { return !!this.consent.get('enabled').value; }
	get iconForm(): UntypedFormGroup { return this.definition.get('iconConfiguration') as UntypedFormGroup; }
	get hasPlugins(): boolean { return (this.editor.plugins?.length ?? 0) > 0; }

	get rows() { return this.editor.editorRows(); }
	/** "Maria Papadopoulou, Nikos Georgiou and Eleni Karra", or the first names and how many more. */
	get namesSentence(): string {
		const names = this.rows.map(r => (this.editor.isMe(r.userId) ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.EDITORS.YOU-NAMED', { name: r.name }) : r.name) || '—');
		if (names.length <= 4) return this.editor.nameList(names);
		return this.editor.nameList([...names.slice(0, 3), this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.EDITORS.N-MORE', { count: names.length - 3 })]);
	}
	/** "1 owner · 2 members · you are the owner" */
	get rolesSummary(): string {
		const t = (k: string, p?: object) => this.editor.language.instant(k, p);
		const parts = [t(this.editor.ownerCount === 1 ? 'PLAN-BLUEPRINT-EDITOR.EDITORS.OWNERS-ONE' : 'PLAN-BLUEPRINT-EDITOR.EDITORS.OWNERS', { count: this.editor.ownerCount }), t(this.editor.memberCount === 1 ? 'PLAN-BLUEPRINT-EDITOR.EDITORS.MEMBERS-ONE' : 'PLAN-BLUEPRINT-EDITOR.EDITORS.MEMBERS', { count: this.editor.memberCount })];
		const mine = this.editor.myRole;
		if (mine != null) parts.push(t(mine === UserPlanBlueprintRole.Owner ? 'PLAN-BLUEPRINT-EDITOR.EDITORS.YOU-ARE-OWNER' : 'PLAN-BLUEPRINT-EDITOR.EDITORS.YOU-ARE-MEMBER'));
		return parts.join(' · ');
	}

	initials(name: string): string { return (name ?? '').split(/\s+/).filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join('') || '?'; }

	/** Editors are managed in a side panel; the changes go into the form and are saved with the blueprint. */
	openEditors(): void {
		this.dialog.open(EditorsPanelComponent, {
			data: { editor: this.editor },
			position: { right: '0', top: '0' },
			height: '100vh',
			width: 'min(440px, 100vw)',
			maxWidth: '100vw',
			panelClass: 'dt2-side-panel',
			autoFocus: 'first-tabbable'
		});
	}

	descriptionSnippet(): string {
		const html: string = this.form.get('description').value ?? '';
		const text = html.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
		return text.length > 180 ? text.slice(0, 180) + '…' : text;
	}
}
