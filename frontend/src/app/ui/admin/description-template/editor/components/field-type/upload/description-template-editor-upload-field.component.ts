import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormArray, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateUploadOption } from '@app/core/model/description-template/description-template';
import { ConfigurationService } from "@app/core/services/configuration/configuration.service";
import { DescriptionTemplateUploadDataEditorModel, DescriptionTemplateUploadOptionEditorModel } from '../../../description-template-editor.model';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';

@Component({
	selector: 'app-description-template-editor-upload-field-component',
	styleUrls: ['./description-template-editor-upload-field.component.scss'],
	templateUrl: './description-template-editor-upload-field.component.html'
})
export class DescriptionTemplateEditorUploadFieldComponent implements OnInit {
	types: Array<DescriptionTemplateUploadOption> = [
		// images
		{ label: "Animated Portable Network Graphics (APNG)", value: "image/apng" },
		{ label: "AV1 Image File Format (AVIF)", value: "image/avif" },
		{ label: "Graphics Interchange Format (GIF)", value: "image/gif" },
		{ label: "Joint Photographic Expert Group image (JPEG)", value: "image/jpeg" },
		{ label: "Portable Network Graphics (PNG)", value: "image/png" },
		{ label: "Scalable Vector Graphics (SVG)", value: "image/svg+xml" },
		{ label: "Web Picture format (WEBP)", value: "image/webp" },
		{ label: "Tagged Image File Format (TIFF)", value: "image/tiff" },

		// office word
		{ label: "Microsoft Word 97-2003", value: "application/msword" },	// .doc, .dot
		{ label: "Microsoft Word 2007-2013", value: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" }, // .docx
		{ label: "OpenDocument Text", value: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" }, // .odt

		// office excel
		{ label: "Microsoft Excel 97-2003", value: "application/vnd.ms-excel" },	// .xls, .xlt, .xla
		{ label: "Microsoft Excel 2007-2013", value: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" }, // .xlsx
		{ label: "OpenDocument Spreadsheet", value: "application/vnd.oasis.opendocument.spreadsheet" }, // .ods

		// office powerpoint
		{ label: "Microsoft PowerPoint 97-2003", value: "application/vnd.ms-powerpoint" },	// .ppt, .pot, .pps, .ppa
		{ label: "Microsoft PowerPoint 2007-2013", value: "application/vnd.openxmlformats-officedocument.presentationml.presentation" }, // .pptx
		{ label: "OpenDocument Presentation", value: "application/vnd.oasis.opendocument.presentation" }, // .odp


		{ label: "Comma-Seperated Values (CSV)", value: "text/csv" },
		{ label: "Adobe Portable Document Format (PDF)", value: "application/pdf" }
	];

	selected: string[] = [];
	public typesFormControl = new UntypedFormControl();

	@Input() form: UntypedFormGroup;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() validationRootPath: string;

	constructor(private configurationService: ConfigurationService) { }

	ngOnInit() {
		let typeValues: string[] = this.types.map(type => type.value);
		if (this.form.get('data') && this.form.get('data').get('types')) {
			for (let type of this.form.get('data').get('types').value) {
				if (typeValues.indexOf(type.value) != -1) {
					this.selected.push(type.value);
				}
			}
			this.typesFormControl.setValue(this.selected);

		}
	}

	selectedType(type: DescriptionTemplateUploadOption) {
		if (!this.form.get('data').get('types').disabled) {
			let index = this.selected.indexOf(type.value);
			if (index == -1) {
				this.selected.push(type.value);
				this.addNewRow(type);
			} else {
				this.selected.splice(index, 1);
				this.deleteRow(index);
			}
		}
	}

	isCustomType(value: string) {
		return this.selected.indexOf(value) == -1;
	}

	addNewRow(type: DescriptionTemplateUploadOption = null) {
		const typesArray = this.form.get('data').get('types') as UntypedFormArray;
		const typeListOptions: DescriptionTemplateUploadOptionEditorModel = new DescriptionTemplateUploadOptionEditorModel(this.validationErrorModel);
		if (type != null) {
			typeListOptions.fromModel(type);
		}
		(<UntypedFormGroup>this.form.get('data')).addControl('types', new UntypedFormBuilder().array([]));
		typesArray.push(typeListOptions.buildForm({rootPath: this.validationRootPath + 'data.types[' + typesArray.length + '].'}));
	}

	deleteRow(index: number) {
		if (this.form.get('data').get('types')) { 
			(<UntypedFormArray>this.form.get('data').get('types')).removeAt(index); 
			DescriptionTemplateUploadDataEditorModel.reapplyUploadDataValidators(
				{
					formGroup: this.form.get('data') as UntypedFormGroup,
					validationErrorModel: this.validationErrorModel,
					rootPath: `${this.validationRootPath}data.`,
				}
			);

			this.form.get('data').get('types').markAsDirty();
		}
	}

	public getConfiguration() {
		return this.configurationService;
	}
}
