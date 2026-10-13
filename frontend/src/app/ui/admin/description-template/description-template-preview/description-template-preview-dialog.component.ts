import {Component, Inject, OnInit, ChangeDetectionStrategy} from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ProgressIndicationService } from '@app/core/services/progress-indication/progress-indication-service';
import { DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { FormScrollService } from '@app/ui/description/editor/description-form/components/services/form-scroll.service';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { DescriptionEditorHelper } from '@app/ui/plan/plan-editor-blueprint/plan-description-editor/plan-description-editor-helper';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatProgressBar } from '@angular/material/progress-bar';
import { DescriptionFormComponent } from '../../../description/editor/description-form/description-form.component';

@Component({
    selector: 'app-description-template-preview-dialog-component',
    templateUrl: 'description-template-preview-dialog.component.html',
    styleUrls: ['./description-template-preview-dialog.component.scss'],
    providers: [FormScrollService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatTooltip, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, MatProgressBar, DescriptionFormComponent, FormsModule, ReactiveFormsModule, MatDialogActions, MatButton, TranslatePipe]
})
export class DescriptionTemplatePreviewDialogComponent extends BaseComponent implements OnInit {
   	showSelect: boolean = true;
	descriptionTemplateDefinitionFormGroup: UntypedFormGroup;
	progressIndication = false;
	editorModel: DescriptionEditorModel;
	formGroup: UntypedFormGroup;
	previewPropertiesFormGroup: UntypedFormGroup;
	descriptionTemplate: DescriptionTemplate;

	constructor(
		public dialogRef: MatDialogRef<DescriptionTemplatePreviewDialogComponent>,
		private progressIndicationService: ProgressIndicationService,
		private descriptionTemplateService: DescriptionTemplateService,
		private uiNotificationService: UiNotificationService,
		private logger: LoggingService,
		private language: TranslateService,
		public visibilityRulesService: VisibilityRulesService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		super();
	}

	ngOnInit(): void {
		this.progressIndicationService.getProgressIndicationObservable().pipe(takeUntil(this._destroyed)).subscribe(x => {
			setTimeout(() => { this.progressIndication = x; });
		});

		if (this.data && this.data.descriptionTemplateId) {

			this.descriptionTemplateService.getSingle(this.data.descriptionTemplateId, DescriptionEditorHelper.DescriptionTemplateLookupFields())
				.pipe(takeUntil(this._destroyed))
				.subscribe(item => {
					this.descriptionTemplate = item;
					this.prepareForm(this.descriptionTemplate);
				}, error => {
					this.dialogRef.close();
					this.httpErrorHandlingService.handleBackedRequestError(error);
				});
		}
		if (this.data && this.data.showSelect == false){
			this.showSelect = false;
		}
	}

	prepareForm(data: DescriptionTemplate) {
		try {
			const mockDescription: Description = {
				descriptionTemplate: this.descriptionTemplate
			}
			this.editorModel = new DescriptionEditorModel().fromModel(mockDescription, mockDescription.descriptionTemplate);;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse Description item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, false, this.visibilityRulesService);
		this.previewPropertiesFormGroup = this.editorModel.properties.buildForm({visibilityRulesService: this.visibilityRulesService}) as UntypedFormGroup;
		this.visibilityRulesService.setContext(this.descriptionTemplate.definition, this.previewPropertiesFormGroup);
	}

	select(): void {
		this.dialogRef.close(this.descriptionTemplate);
	}

	closeDialog(event: MouseEvent): void {
		if (event != null) {
			event.stopPropagation();
			event.preventDefault();
		}
		this.dialogRef.close();
	}

}
