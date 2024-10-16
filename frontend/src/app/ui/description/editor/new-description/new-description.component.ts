import { Component, Inject, OnInit } from "@angular/core";
import { UntypedFormGroup } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { DescriptionPrefillingRequest, PrefillingSearchRequest } from "@app/core/model/description-prefilling-request/description-prefilling-request";
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";
import { Description } from "@app/core/model/description/description";
import { Plan } from "@app/core/model/plan/plan";
import { Prefilling } from "@app/core/model/prefilling-source/prefilling-source";
import { PrefillingSourceService } from "@app/core/services/prefilling-source/prefilling-source.service";
import { ProgressIndicationService } from "@app/core/services/progress-indication/progress-indication-service";
import { SingleAutoCompleteConfiguration } from "@app/library/auto-complete/single/single-auto-complete-configuration";
import { BaseComponent } from "@common/base/base.component";
import { FormService } from "@common/forms/form-service";
import { HttpErrorHandlingService } from "@common/modules/errors/error-handling/http-error-handling.service";
import { Guid } from "@common/types/guid";
import { Observable } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { DescriptionEditorEntityResolver } from "../resolvers/description-editor-entity.resolver";
import { DescriptionPrefillingRequestEditorModel } from "./new-description-editor.model";

@Component({
	selector: 'new-description-component',
	templateUrl: 'new-description.component.html',
	styleUrls: ['new-description.component.scss']
})
export class NewDescriptionDialogComponent extends BaseComponent implements OnInit {

	progressIndication = false;
	singlePrefillingSourceAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	prefillObjectAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	prefillSelected: boolean = false;
	prefillForm: UntypedFormGroup;

	plan: Plan;
	planSectionId: Guid;
	prefillingSourcesEnabled: boolean;
	availableDescriptionTemplates: DescriptionTemplate[] = [];

	constructor(public dialogRef: MatDialogRef<NewDescriptionDialogComponent>,
		private progressIndicationService: ProgressIndicationService,
		public prefillingSourceService: PrefillingSourceService,
		private formService: FormService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		@Inject(MAT_DIALOG_DATA) public data: any) {
		super();

		this.plan = data.plan;
		this.planSectionId = data.planSectionId;

		const filteredSections = data.plan?.blueprint.definition.sections.filter(x => x.id == this.planSectionId);
		this.prefillingSourcesEnabled = filteredSections && filteredSections?.length > 0 ? filteredSections[0].prefillingSourcesEnabled : false;

		this.availableDescriptionTemplates = this.plan.planDescriptionTemplates.filter(x => x.sectionId == this.planSectionId && x.isActive == IsActive.Active).map(x => x.currentDescriptionTemplate);
	}

	ngOnInit() {
		const availablePrefillingSourcesIds = this.plan.blueprint.definition.sections.filter(x => x.id === this.planSectionId)[0].prefillingSources?.map(x => x.id) || null;
		this.singlePrefillingSourceAutoCompleteConfiguration = this.prefillingSourceService.getSingleAutocompleteConfiguration(availablePrefillingSourcesIds);

		this.progressIndicationService.getProgressIndicationObservable().pipe(takeUntil(this._destroyed)).subscribe(x => {
			setTimeout(() => { this.progressIndication = x; });
		});
		const editorModel = new DescriptionPrefillingRequestEditorModel();
		this.prefillForm = editorModel.buildForm(null, false);

		this.prefillObjectAutoCompleteConfiguration = {
			filterFn: this.searchDescriptions.bind(this),
			loadDataOnStart: false,
			displayFn: (item: Prefilling) => (item.label.length > 60) ? (item.label.substr(0, 60) + "...") : item.label,
			titleFn: (item: Prefilling) => item.label,
			subtitleFn: (item: Prefilling) => item.id,
			valueAssign: (item: Prefilling) => item,
			uniqueAssign: (item: Prefilling) => item.id,
		};
	}

	changePreffillingSource() {
		this.prefillForm.get('data').setValue(null);
	}

	public compareWith(object1: any, object2: any) {
		return object1 && object2 && object1.id === object2.id;
	}

	searchDescriptions(query: string): Observable<Prefilling[]> {
		const request: PrefillingSearchRequest = {
			like: query,
			prefillingSourceId: this.prefillForm.get('prefillingSourceId').value
		};

		return this.prefillingSourceService.search(request);
	}

	next() {
		const formData = this.formService.getValue(this.prefillForm.value) as DescriptionPrefillingRequest;

		this.prefillingSourceService.generate(formData, DescriptionEditorEntityResolver.descriptionTemplateLookupFieldsForDescrption())
			.pipe(takeUntil(this._destroyed)).subscribe(description => {
				if (description) {
					this.closeDialog({ description: description });
				} else {
					this.closeDialog();
				}
			},
				error => {
					this.httpErrorHandlingService.handleBackedRequestError(error);
					this.dialogRef.close();
				});
	}

	manuallySelected() {
		if (!this.prefillForm.get('descriptionTemplateId').valid) return;
		this.closeDialog({ descriptionTemplateId: this.prefillForm.get('descriptionTemplateId').value });
	}

	closeDialog(result = null): void {
		this.dialogRef.close(result);
	}
}

export class NewDescriptionDialogComponentResult {
	description: Description;
	descriptionTemplateId: Guid;
}
