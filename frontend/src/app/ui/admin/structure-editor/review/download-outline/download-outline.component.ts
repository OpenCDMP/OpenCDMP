import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray } from '@angular/forms';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

interface DownloadQuestion { numbering: string, title: string, included: boolean, partial: boolean, conditional: boolean }
interface DownloadSection { numbering: string, title: string, depth: number, assets: boolean, questions: DownloadQuestion[] }

/** Every section and question in the order of a download, with what the download leaves out. */
@Component({
    selector: 'app-se2-download-outline',
    templateUrl: './download-outline.component.html',
    styleUrls: ['./download-outline.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, TranslatePipe]
})
export class DownloadOutlineComponent {
	constructor(public editor: StructureEditorBase<any, any>) { }

	get exportCustomized(): boolean {
		return ((this.editor.formGroup.controls.definition.get('pluginConfigurations') as UntypedFormArray)?.controls ?? [])
			.some(p => ((p.get('fields') as UntypedFormArray)?.controls ?? []).some(f => !!f.get('fileValue')?.value || (f.get('textValue')?.value ?? '') !== ''));
	}

	get sections(): DownloadSection[] {
		const result: DownloadSection[] = [];
		const visit = (list: ToCEntry[], depth: number) => list?.forEach(e => {
			if (e.type !== ToCEntryType.Section) return;
			const questions = (e.subEntries ?? []).filter(q => q.type === ToCEntryType.FieldSet).map(q => {
				const fields = ((q.form.get('fields') as UntypedFormArray)?.controls ?? []);
				const inCount = fields.filter(f => !!f.get('includeInExport')?.value).length;
				return { numbering: q.numbering, title: q.label, included: inCount > 0, partial: inCount > 0 && inCount < fields.length, conditional: this.editor.rulesInto(q.form as any).length > 0 };
			});
			result.push({ numbering: e.numbering, title: e.label, depth, assets: !!e.root && !!e.form.get('hasTemplates')?.value, questions });
			visit(e.subEntries, depth + 1);
		});
		visit(this.editor.toCEntries, 0);
		return result;
	}
}
