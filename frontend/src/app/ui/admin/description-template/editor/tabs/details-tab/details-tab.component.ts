import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UserDescriptionTemplateRole } from '@app/core/common/enum/user-description-template-role';
import { MatDialog } from '@angular/material/dialog';
import { EditorsPanelComponent } from '../../editors-panel/editors-panel.component';
import { DescriptionTemplateEditorComponent } from '../../description-template-editor.component';
import { AboutCardComponent } from '../../../../structure-editor/details/about-card/about-card.component';
import { MatFormField, MatLabel, MatHint, MatError } from '@angular/material/form-field';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { ExportCardComponent } from '../../../../structure-editor/details/export-card/export-card.component';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../../../structure-editor/count.pipe';

/**
 * Details tab: what the template is, who may change it, how it exports, and what is left before it can be finalized.
 * The About, export and readiness cards are shared with the plan blueprint editor.
 */
@Component({
    selector: 'app-dt2-details-tab',
    templateUrl: './details-tab.component.html',
    styleUrls: ['./details-tab.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AboutCardComponent, MatFormField, MatLabel, MatSelect, FormsModule, ReactiveFormsModule, MatOption, MatHint, MatError, MatButton, MatIcon, ExportCardComponent, TranslatePipe, Bp2CountPipe]
})
export class Dt2DetailsTabComponent {
	constructor(public editor: DescriptionTemplateEditorComponent, private dialog: MatDialog) { }

	get form(): UntypedFormGroup { return this.editor.formGroup as unknown as UntypedFormGroup; }
	get definition(): UntypedFormGroup { return this.form.get('definition') as UntypedFormGroup; }
	get hasPlugins(): boolean { return (this.editor.plugins?.length ?? 0) > 0; }
	get languageName(): string { return this.editor.languageName; }
	get fieldCount(): number { return this.editor.allFields().length; }

	get rows() { return this.editor.editorRows(); }
	/** "Maria Papadopoulou, Nikos Georgiou and Eleni Karra", or the first names and how many more. */
	get namesSentence(): string {
		const names = this.rows.map(r => (this.editor.isMe(r.userId) ? this.editor.language.instant('DESCRIPTION-TEMPLATE-EDITOR.EDITORS.YOU-NAMED', { name: r.name }) : r.name) || '—');
		if (names.length <= 4) return this.editor.nameList(names);
		return this.editor.nameList([...names.slice(0, 3), this.editor.language.instant('DESCRIPTION-TEMPLATE-EDITOR.EDITORS.N-MORE', { count: names.length - 3 })]);
	}
	/** "1 owner · 2 members · you are the owner" */
	get rolesSummary(): string {
		const t = (k: string, p?: object) => this.editor.language.instant(k, p);
		const parts = [t(this.editor.ownerCount === 1 ? 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.OWNERS-ONE' : 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.OWNERS', { count: this.editor.ownerCount }), t(this.editor.memberCount === 1 ? 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.MEMBERS-ONE' : 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.MEMBERS', { count: this.editor.memberCount })];
		const mine = this.editor.myRole;
		if (mine != null) parts.push(t(mine === UserDescriptionTemplateRole.Owner ? 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.YOU-ARE-OWNER' : 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.YOU-ARE-MEMBER'));
		return parts.join(' · ');
	}

	initials(name: string): string { return (name ?? '').split(/\s+/).filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join('') || '?'; }

	/** Editors are managed in a side panel; the changes go into the form and are saved with the template. */
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
