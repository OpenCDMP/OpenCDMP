import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { FormScrollService } from '@app/ui/description/editor/description-form/components/services/form-scroll.service';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { DescriptionEditorHelper } from '@app/ui/plan/plan-editor-blueprint/plan-description-editor/plan-description-editor-helper';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatCard, MatCardContent } from '@angular/material/card';
import { DescriptionFormComponent } from '../../../description/editor/description-form/description-form.component';

// Read-only, full-page preview of a description template, reusing the same form-rendering
// pipeline as the admin template-preview dialog (mock Description + app-description-form).
@Component({
    selector: 'app-description-template-library-view',
    templateUrl: './description-template-library-view.component.html',
    styleUrls: ['./description-template-library-view.component.scss'],
    providers: [FormScrollService, VisibilityRulesService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButton, MatIcon, MatCard, MatCardContent, DescriptionFormComponent, FormsModule, ReactiveFormsModule, TranslatePipe]
})
export class DescriptionTemplateLibraryViewComponent extends BaseComponent implements OnInit {
    editorModel: DescriptionEditorModel;
    formGroup: UntypedFormGroup;
    previewPropertiesFormGroup: UntypedFormGroup;
    descriptionTemplate: DescriptionTemplate;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private descriptionTemplateService: DescriptionTemplateService,
        private uiNotificationService: UiNotificationService,
        private logger: LoggingService,
        private language: TranslateService,
        public visibilityRulesService: VisibilityRulesService,
        private httpErrorHandlingService: HttpErrorHandlingService,
        private routerUtils: RouterUtilsService,
    ) {
        super();
    }

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (!id) { this.back(); return; }
        this.descriptionTemplateService.getSingle(Guid.parse(id), DescriptionEditorHelper.DescriptionTemplateLookupFields())
            .pipe(takeUntil(this._destroyed))
            .subscribe(item => {
                this.descriptionTemplate = item;
                this.prepareForm(item);
            }, error => {
                this.httpErrorHandlingService.handleBackedRequestError(error);
                this.back();
            });
    }

    private prepareForm(data: DescriptionTemplate) {
        try {
            const mockDescription: Description = { descriptionTemplate: data };
            this.editorModel = new DescriptionEditorModel().fromModel(mockDescription, mockDescription.descriptionTemplate);
            this.formGroup = this.editorModel.buildForm(null, false, this.visibilityRulesService);
            this.previewPropertiesFormGroup = this.editorModel.properties.buildForm({ visibilityRulesService: this.visibilityRulesService }) as UntypedFormGroup;
            this.visibilityRulesService.setContext(data.definition, this.previewPropertiesFormGroup);
        } catch (error) {
            this.logger.error('Could not parse description template: ' + error);
            this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
        }
    }

    back() {
        this.router.navigate([this.routerUtils.generateUrl('/asset-template-library')]);
    }
}
