import {Component, computed, OnInit, ViewChild, ChangeDetectionStrategy} from "@angular/core";
import {PlanBlueprintService} from "@app/core/services/plan/plan-blueprint.service";
import {map, takeUntil} from "rxjs/operators";
import {BaseComponent} from "@common/base/base.component";
import {DescriptionTemplateService} from "@app/core/services/description-template/description-template.service";
import {HttpErrorHandlingService} from "@common/modules/errors/error-handling/http-error-handling.service";
import { EnumUtils } from "@app/core/services/utilities/enum-utils.service";
import { ActivatedRoute, Router } from "@angular/router";
import { PlanUpdateRequestService } from "@app/core/services/plan/plan-update-request.service";
import { PlanSuggestion, PlanUpdateRequest, PlanUpdateRequestApply } from "@app/core/model/plan-update-request/plan-update-request";
import { PlanUpdateRequestApplyDecision } from "@app/core/common/enum/plan-update-request-apply-decision.enum";
import { SnackBarNotificationLevel, UiNotificationService } from "@app/core/services/notification/ui-notification-service";
import { HttpErrorResponse } from "@angular/common/http";
import { RouterUtilsService } from "@app/core/services/router/router-utils.service";
import { PlanSuggestionModel } from "./plan-suggestion.model";
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { DescriptionTemplatePreviewDialogComponent } from "@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component";
import { MatDialog } from "@angular/material/dialog";
import { PlanBlueprint, RootSection } from "@app/core/model/plan-blueprint/plan-blueprint";
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";
import { SingleAutoCompleteConfiguration } from "@app/library/auto-complete/single/single-auto-complete-configuration";
import { DescriptionTemplateVersionStatus } from "@app/core/common/enum/description-template-version-status";
import { DescriptionTemplateStatus } from "@app/core/common/enum/description-template-status";
import { FormService } from "@common/forms/form-service";
import { EnqueueService } from "@app/core/services/enqueue.service";
import { PlanDescriptionEditorComponent } from "../plan-editor-blueprint/plan-description-editor/plan-description-editor.component";
import { PlanTableOfContentsComponent } from "../plan-editor-blueprint/plan-table-of-contents/plan-table-of-contents.component";
import { TranslateService, TranslatePipe } from "@ngx-translate/core";
import { MatCard, MatCardContent, MatCardActions } from "@angular/material/card";
import { MatFormField, MatLabel, MatError } from "@angular/material/form-field";
import { SingleAutoCompleteComponent } from "../../../library/auto-complete/single/single-auto-complete.component";
import { MatIcon } from "@angular/material/icon";
import { MatSelect, MatOption } from "@angular/material/select";
import { MatButton } from "@angular/material/button";

@Component({
    selector: 'app-plan-suggestion-component',
    templateUrl: './plan-suggestion.component.html',
    styleUrls: ['./plan-suggestion.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatCard, MatCardContent, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, SingleAutoCompleteComponent, MatIcon, MatError, MatSelect, MatOption, MatCardActions, MatButton, TranslatePipe]
})

export class PlanSuggestionComponent extends BaseComponent implements OnInit{
    
	@ViewChild('descriptionEditor') descriptionEditor: PlanDescriptionEditorComponent;
	@ViewChild('tableOfContent') tableOfContent: PlanTableOfContentsComponent;

	planBlueprints: any[] = [];
	formGroup: UntypedFormGroup;
	selectedBlueprintSections: RootSection[];

	descriptionTemplateSingleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildAutocompleteLookup(null, null, null, [DescriptionTemplateVersionStatus.Current], [DescriptionTemplateStatus.Finalized])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildAutocompleteLookup(searchQuery, null, null, [DescriptionTemplateVersionStatus.Current], [DescriptionTemplateStatus.Finalized])).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildAutocompleteLookup(null, null, [selectedItem], [DescriptionTemplateVersionStatus.Current], [DescriptionTemplateStatus.Finalized])).pipe(map(x => x.items[0])),
		displayFn: (item: DescriptionTemplate) => item.label,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: (item: DescriptionTemplate) => item.id,
		popupItemActionIcon: 'visibility'
	};

	isLoading = computed(() => this.enqueueService.exhaustPipelineBusy());
	
	constructor(
		private dialog: MatDialog,
		private router: Router,
		private routerUtils: RouterUtilsService,
        private planUpdateRequestService: PlanUpdateRequestService,
		private route: ActivatedRoute,
		private uiNotificationService: UiNotificationService,
		private formService: FormService,
		private enqueueService: EnqueueService,
		private language: TranslateService,
		public planBlueprintService: PlanBlueprintService,
        public descriptionTemplateService: DescriptionTemplateService,
        protected httpErrorHandlingService: HttpErrorHandlingService,
        protected enumUtils: EnumUtils
    ) {
		super();
	}

	ngOnInit() {
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe(params => {
				const id = params['planUpdateRequestId']
				if (id != null) {
					this.planUpdateRequestService.preprocessing(id)
						.subscribe(result => {
							this.formGroup = new PlanSuggestionModel().fromModel(result, id).buildForm();
						}, error => {
							this.onCallbackError(error);
						});
				}
			});
	}

	selectedBlueprintChanged(item: PlanBlueprint): void{
		this.selectedBlueprintSections = item.definition?.sections?.filter(x => x.hasTemplates) || null;
		if (this.formGroup){
			const descriptionsFormArray = this.formGroup.get('descriptions') as UntypedFormArray;
			descriptionsFormArray.controls.forEach( control =>{
				control.get('sectionId').patchValue(null);
			})
		}
	}

	onPreviewDescriptionTemplate(event, descriptionIndex: number) {
		const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
			width: '590px',
			minHeight: '200px',
			data: {
				descriptionTemplateId: event.id
			},
			panelClass: 'custom-modalbox'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(descTemplate => {
			if (descTemplate) {
				(this.formGroup.get('descriptions') as UntypedFormArray).at(descriptionIndex).get('templateId').patchValue(event.id);
			}
		});
	}

	confirm() {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (this.formGroup.valid){
			const planSuggestion = this.formService.getValue(this.formGroup.value) as PlanSuggestion;
			const apply: PlanUpdateRequestApply = {
				planUpdateRequestId: planSuggestion.planUpdateRequestId,
				decision: PlanUpdateRequestApplyDecision.Accept,
				suggestion: planSuggestion,
			};
			this.planUpdateRequestService.applyCreate(apply)
				.subscribe(result => {
					this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-UPLOAD.UPLOAD-SUCCESS'), SnackBarNotificationLevel.Success);

                    this.router.navigateByUrl('/reload', { skipLocationChange: true }).then(() => {
                        setTimeout(() => this.router.navigate([this.routerUtils.generateUrl('/plans')]));
                    });
				}, error => {
					this.onCallbackError(error);
				});
		} else {
			return;
		}
	}

	onCallbackError(errorResponse?: HttpErrorResponse) {

		if (!errorResponse) {
			this.uiNotificationService.snackBarNotification('GENERAL.SNACK-BAR.GENERIC-ERROR', SnackBarNotificationLevel.Error);
			return;
		}
		
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse)
		this.router.navigate([this.routerUtils.generateUrl('home')]);
	}
	
	

}
