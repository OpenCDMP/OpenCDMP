import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldUploadDataEditorModel, FieldUploadOptionEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { StructureEditorBase } from '../../structure-editor-base';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatButton } from '@angular/material/button';
import { MatDivider } from '@angular/material/list';
import { TranslatePipe } from '@ngx-translate/core';

interface KnownType { short: string, label: string, value: string, group: 'DOCS' | 'SHEETS' | 'SLIDES' | 'IMAGES' | 'DATA' }

/**
 * The same list of media types the old upload editor offers, with a short name for the chips. Labels and values
 * are stored exactly as before; the old list's OpenDocument Text entry wrongly reused the DOCX type, fixed here.
 */
const KNOWN_TYPES: KnownType[] = [
	{ short: 'PDF', label: 'Adobe Portable Document Format (PDF)', value: 'application/pdf', group: 'DOCS' },
	{ short: 'Word 97-2003', label: 'Microsoft Word 97-2003', value: 'application/msword', group: 'DOCS' },
	{ short: 'Word', label: 'Microsoft Word 2007-2013', value: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', group: 'DOCS' },
	{ short: 'ODT', label: 'OpenDocument Text', value: 'application/vnd.oasis.opendocument.text', group: 'DOCS' },
	{ short: 'Excel 97-2003', label: 'Microsoft Excel 97-2003', value: 'application/vnd.ms-excel', group: 'SHEETS' },
	{ short: 'Excel', label: 'Microsoft Excel 2007-2013', value: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', group: 'SHEETS' },
	{ short: 'ODS', label: 'OpenDocument Spreadsheet', value: 'application/vnd.oasis.opendocument.spreadsheet', group: 'SHEETS' },
	{ short: 'CSV', label: 'Comma-Seperated Values (CSV)', value: 'text/csv', group: 'DATA' },
	{ short: 'PowerPoint 97-2003', label: 'Microsoft PowerPoint 97-2003', value: 'application/vnd.ms-powerpoint', group: 'SLIDES' },
	{ short: 'PowerPoint', label: 'Microsoft PowerPoint 2007-2013', value: 'application/vnd.openxmlformats-officedocument.presentationml.presentation', group: 'SLIDES' },
	{ short: 'ODP', label: 'OpenDocument Presentation', value: 'application/vnd.oasis.opendocument.presentation', group: 'SLIDES' },
	{ short: 'PNG', label: 'Portable Network Graphics (PNG)', value: 'image/png', group: 'IMAGES' },
	{ short: 'JPEG', label: 'Joint Photographic Expert Group image (JPEG)', value: 'image/jpeg', group: 'IMAGES' },
	{ short: 'GIF', label: 'Graphics Interchange Format (GIF)', value: 'image/gif', group: 'IMAGES' },
	{ short: 'SVG', label: 'Scalable Vector Graphics (SVG)', value: 'image/svg+xml', group: 'IMAGES' },
	{ short: 'WEBP', label: 'Web Picture format (WEBP)', value: 'image/webp', group: 'IMAGES' },
	{ short: 'TIFF', label: 'Tagged Image File Format (TIFF)', value: 'image/tiff', group: 'IMAGES' },
	{ short: 'AVIF', label: 'AV1 Image File Format (AVIF)', value: 'image/avif', group: 'IMAGES' },
	{ short: 'APNG', label: 'Animated Portable Network Graphics (APNG)', value: 'image/apng', group: 'IMAGES' },
];

/** File upload settings: the size limit and the allowed types as chips, with a way to add any other media type. */
@Component({
    selector: 'app-bp2-upload-settings',
    templateUrl: './upload-settings.component.html',
    styleUrls: ['./upload-settings.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ReactiveAsteriskDirective, FormsModule, ReactiveFormsModule, MatTooltip, MatIcon, MatMenuTrigger, MatButton, MatMenu, MatMenuItem, MatDivider, TranslatePipe]
})
export class Bp2UploadSettingsComponent {
	@Input() fieldForm: UntypedFormGroup;
	@Input() rootPath = '';

	readonly groups: KnownType['group'][] = ['DOCS', 'SHEETS', 'SLIDES', 'IMAGES', 'DATA'];
	showCustom = false;
	customLabel = '';
	customValue = '';

	constructor(public editor: StructureEditorBase<any, any>) { }

	get readOnly(): boolean { return this.editor.readOnly; }
	get data(): UntypedFormGroup { return this.fieldForm?.get('data') as UntypedFormGroup; }
	get types(): UntypedFormArray { return this.data?.get('types') as UntypedFormArray; }
	get maxAllowed(): number { return this.editor.configurationService.maxFileSizeInMB; }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }

	knownIn(group: KnownType['group']): KnownType[] { return KNOWN_TYPES.filter(t => t.group === group); }
	isSelected(value: string): boolean { return (this.types?.value ?? []).some((t: { value: string }) => t.value === value); }
	shortName(type: { label: string, value: string }): string { return KNOWN_TYPES.find(k => k.value === type.value)?.short ?? type.label ?? type.value; }

	add(type: { label: string, value: string }): void {
		if (this.readOnly || !type.value || this.isSelected(type.value)) return;
		const index = this.types.length;
		this.types.push(new FieldUploadOptionEditorModel(this.editor.editorModel.validationErrorModel).fromModel(type).buildForm({ rootPath: `${this.rootPath}data.types[${index}].` }));
		this.types.markAsDirty();
		this.types.updateValueAndValidity();
	}

	toggleKnown(type: KnownType): void {
		const index = (this.types?.value ?? []).findIndex((t: { value: string }) => t.value === type.value);
		if (index >= 0) this.remove(index); else this.add({ label: type.label, value: type.value });
	}

	remove(index: number): void {
		if (this.readOnly) return;
		this.types.removeAt(index);
		FieldUploadDataEditorModel.reapplyUploadDataValidators({ formGroup: this.data, validationErrorModel: this.editor.editorModel.validationErrorModel, rootPath: `${this.rootPath}data.` });
		this.types.markAsDirty();
		this.types.updateValueAndValidity();
	}

	addCustom(): void {
		const value = this.customValue.trim();
		if (!value) return;
		this.add({ label: this.customLabel.trim() || value, value });
		this.customLabel = ''; this.customValue = ''; this.showCustom = false;
	}
}
