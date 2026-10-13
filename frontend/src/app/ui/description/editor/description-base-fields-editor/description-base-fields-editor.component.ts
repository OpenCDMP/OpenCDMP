import { Component, Optional, computed, effect, EventEmitter, input, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionService } from '@app/core/services/description/description.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { PlanTempStorageService } from '@app/ui/plan/plan-editor-blueprint/plan-temp-storage.service';
import { PlanReviewService } from '@app/ui/plan/plan-editor-blueprint/plan-review.service';
import { DescriptionContextService } from '@app/ui/plan/plan-editor-blueprint/plan-description-editor/description-context.service';
import { FieldChange, FieldValuePreview } from '@app/core/model/plan-update-request/plan-update-preview';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { FieldChangeBadgeComponent } from '../../../plan/plan-editor-blueprint/field-change-badge/field-change-badge.component';
import { MatFormField, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { RichTextEditorComponent } from '../../../../library/rich-text-editor/rich-text-editor.component';
import { TagsComponent } from '../../../tag/tags-field/tags-field.component';

@Component({
    selector: 'app-description-base-fields-editor-component',
    templateUrl: 'description-base-fields-editor.component.html',
    styleUrls: ['./description-base-fields-editor.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormsModule, ReactiveFormsModule, FieldChangeBadgeComponent, MatFormField, MatInput, MatError, RichTextEditorComponent, TagsComponent, TranslatePipe]
})
export class DescriptionBaseFieldsEditorComponent extends BaseComponent {
    fields = input<SystemFieldType[]>([SystemFieldType.PlanDescriptionTitle, SystemFieldType.PlanDescriptionDescription, SystemFieldType.PlanDescriptionTag])

	descriptionId = input<Guid>(null);
    description = computed(() => this.planTempStorage.descriptions()?.get(this.descriptionId()?.toString())?.lastPersist);
    formGroup = computed(() => this.planTempStorage.descriptions()?.get(this.descriptionId()?.toString())?.formGroup);
	availableDescriptionTemplates: DescriptionTemplate[] = [];

	@Output() refresh: EventEmitter<any> = new EventEmitter<any>();

    SystemFieldType = SystemFieldType;

    // System field change badges
    readonly labelChange = computed<FieldChange | null>(() => this.systemFieldChange('system.label'));
    readonly descriptionChange = computed<FieldChange | null>(() => this.systemFieldChange('system.description'));
    readonly tagsChange = computed<FieldChange | null>(() => this.systemFieldChange('system.tags'));

    private systemFieldChange(fieldId: string): FieldChange | null {
        if (!this.planReviewService || !this.descriptionContextService) return null;
        const descId = this.descriptionContextService.descriptionId();
        if (!descId) return null;
        return this.planReviewService.descriptionFieldChangeByKey().get(`${descId}:${fieldId}`) ?? null;
    }

    readLabel = () => this.formGroup()?.get('label')?.value ?? null;
    writeLabel = (value: unknown) => {
        const v = this.extractText(value);
        this.formGroup()?.get('label')?.patchValue(v);
        this.formGroup()?.get('label')?.markAsDirty();
    };

    readDescription = () => this.formGroup()?.get('description')?.value ?? null;
    writeDescription = (value: unknown) => {
        const v = this.extractText(value);
        this.formGroup()?.get('description')?.patchValue(v);
        this.formGroup()?.get('description')?.markAsDirty();
    };

    readTags = () => this.formGroup()?.get('tags')?.value ?? null;
    writeTags = (value: unknown) => {
        const ctrl = this.formGroup()?.get('tags');
        if (!ctrl) return;
        let tags: string[] = [];
        if (value != null && typeof value === 'object' && 'textListValue' in (value as object)) {
            tags = (value as FieldValuePreview).textListValue ?? [];
        } else if (Array.isArray(value)) {
            tags = value;
        }
        ctrl.patchValue(tags);
        ctrl.markAsDirty();
    };

    private extractText(value: unknown): string | null {
        if (value == null) return null;
        if (typeof value === 'string') return value;
        if (typeof value === 'object' && 'textValue' in (value as object)) {
            return (value as FieldValuePreview).textValue ?? null;
        }
        return null;
    }

	constructor(private dialog: MatDialog,
        private planTempStorage: PlanTempStorageService,
		private descriptionService: DescriptionService,
        private language: TranslateService,
        @Optional() private planReviewService: PlanReviewService | null,
        @Optional() private descriptionContextService: DescriptionContextService | null) {
		super();
        // effect(() => {
        //     const description = this.description();
        //     const formGroup = this.formGroup();
        //     if(description){
        //         this.loadDescriptionTemplates();
        //     }
        // })
        // Register display meta for system fields when review is active
        effect(() => {
            if (!this.planReviewService) return;
            const label = this.labelChange();
            const containerLabel = this.description()?.label ?? null;
            if (label?.changeId) this.planReviewService.registerDisplayMeta(label.changeId, {
                ordinal: null,
                label: this.language.instant('DESCRIPTION-EDITOR.BASE-INFO.FIELDS.TITLE'),
                containerLabel,
            });
            const desc = this.descriptionChange();
            if (desc?.changeId) this.planReviewService.registerDisplayMeta(desc.changeId, {
                ordinal: null,
                label: this.language.instant('DESCRIPTION-EDITOR.BASE-INFO.FIELDS.DESCRIPTION'),
                containerLabel,
            });
            const tags = this.tagsChange();
            if (tags?.changeId) this.planReviewService.registerDisplayMeta(tags.changeId, {
                ordinal: null,
                label: this.language.instant('DESCRIPTION-EDITOR.BASE-INFO.FIELDS.TAGS'),
                containerLabel,
            });
        }, { allowSignalWrites: true });
	}

	// public compareWith(value1: string, value2: string): boolean {
	// 	return value1 && value2 && value1 === value2;
	// }

	// private loadDescriptionTemplates(): void {
	// 	const planDescriptionTemplates: PlanDescriptionTemplate[] = this.description().isActive == IsActive.Active ? this.description().plan.planDescriptionTemplates.filter(x => x.sectionId == this.description().planDescriptionTemplate.sectionId && x.isActive == IsActive.Active): this.description().plan.planDescriptionTemplates.filter(x => x.sectionId == this.description().planDescriptionTemplate.sectionId);
	// 	this.availableDescriptionTemplates = planDescriptionTemplates.map(x => x.currentDescriptionTemplate);

	// 	if (this.description()?.descriptionTemplate != null) {
	// 		const isPreviousVersion: boolean = this.description().descriptionTemplate.versionStatus === DescriptionTemplateVersionStatus.Previous;
	// 		if (isPreviousVersion === true) {
	// 			if (this.description().status?.internalStatus === DescriptionStatusEnum.Draft) {
	// 				this.openDeprecatedDescriptionTemplateDialog();
	// 			} else {
	// 				this.availableDescriptionTemplates.push(this.description().descriptionTemplate);
	// 			}
	// 		}
	// 	}
	// }

	// private openDeprecatedDescriptionTemplateDialog(): void {
	// 	const dialogRef = this.dialog.open(DeprecatedDescriptionTemplateDialog, {
	// 		data: {
	// 			label: this.description().descriptionTemplate.label
	// 		}
	// 	});
	// 	dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(
	// 		result => {
	// 			if (result) {
	// 				this.descriptionService.updateDescriptionTemplate({
	// 					id: this.description().id,
	// 					hash: this.description().hash
	// 				})
	// 					.subscribe(
	// 						result => {
	// 							this.refresh.emit();
	// 						},
	// 						error => console.error(error));
	// 			} else {
	// 				this.availableDescriptionTemplates.push(this.description().descriptionTemplate);
	// 			}
	// 		});
	// }
}
