import { Component, Inject, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ProgressIndicationService } from '@app/core/services/progress-indication/progress-indication-service';
import { DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { DescriptionEditorEntityResolver } from '@app/ui/description/editor/resolvers/description-editor-entity.resolver';
import { DescriptionFormService } from '@app/ui/description/editor/description-form/components/services/description-form.service';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';

@Component({
	selector: 'app-description-template-preview-dialog-component',
	templateUrl: 'description-template-preview-dialog.component.html',
	styleUrls: ['./description-template-preview-dialog.component.scss'],
	providers: [DescriptionFormService],
})
export class DescriptionTemplatePreviewDialogComponent extends BaseComponent implements OnInit {

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

			this.descriptionTemplateService.getSingle(this.data.descriptionTemplateId, DescriptionEditorEntityResolver.descriptionTemplateLookupFields())
				.pipe(takeUntil(this._destroyed))
				.subscribe(item => {
					this.descriptionTemplate = item;
					this.prepareForm(this.descriptionTemplate);
				}, error => {
					this.dialogRef.close();
					this.httpErrorHandlingService.handleBackedRequestError(error);
				});
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
		this.formGroup = this.editorModel.buildForm(null, true, this.visibilityRulesService);
		this.previewPropertiesFormGroup = this.editorModel.properties.buildForm({visibilityRulesService: this.visibilityRulesService}) as UntypedFormGroup;
		this.visibilityRulesService.setContext(this.descriptionTemplate.definition, this.previewPropertiesFormGroup);
	}

	select(): void {
		this.dialogRef.close(this.descriptionTemplate.groupId);
	}

	closeDialog(): void {
		this.dialogRef.close();
	}

}
