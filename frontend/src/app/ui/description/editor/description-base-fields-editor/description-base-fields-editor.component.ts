import { Component, computed, effect, EventEmitter, input, Input, Output } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { PlanDescriptionTemplate } from '@app/core/model/plan/plan';
import { DescriptionService } from '@app/core/services/description/description.service';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';
import { DeprecatedDescriptionTemplateDialog } from './dialog-description-template/deprecated-description-template-dialog.component';
import { Subscription } from 'rxjs';
import { MatSelectChange } from '@angular/material/select';
import { Guid } from '@common/types/guid';
import { PlanTempStorageService } from '@app/ui/plan/plan-editor-blueprint/plan-temp-storage.service';

@Component({
    selector: 'app-description-base-fields-editor-component',
    templateUrl: 'description-base-fields-editor.component.html',
    styleUrls: ['./description-base-fields-editor.component.scss'],
    standalone: false
})
export class DescriptionBaseFieldsEditorComponent extends BaseComponent {

	descriptionId = input<Guid>(null);
    description = computed(() => this.planTempStorage.descriptions()?.get(this.descriptionId()?.toString())?.lastPersist);
    formGroup = computed(() => this.planTempStorage.descriptions()?.get(this.descriptionId()?.toString())?.formGroup);
	availableDescriptionTemplates: DescriptionTemplate[] = [];

	@Output() refresh: EventEmitter<any> = new EventEmitter<any>();
    
	constructor(private dialog: MatDialog,
        private planTempStorage: PlanTempStorageService,
		private descriptionService: DescriptionService) {
		super();
        effect(() => {
            const description = this.description();
            const formGroup = this.formGroup();
            if(description){
                this.loadDescriptionTemplates();
            }
        })
	}

	public compareWith(value1: string, value2: string): boolean {
		return value1 && value2 && value1 === value2;
	}

	private loadDescriptionTemplates(): void {
		const planDescriptionTemplates: PlanDescriptionTemplate[] = this.description().isActive == IsActive.Active ? this.description().plan.planDescriptionTemplates.filter(x => x.sectionId == this.description().planDescriptionTemplate.sectionId && x.isActive == IsActive.Active): this.description().plan.planDescriptionTemplates.filter(x => x.sectionId == this.description().planDescriptionTemplate.sectionId);
		this.availableDescriptionTemplates = planDescriptionTemplates.map(x => x.currentDescriptionTemplate);

		if (this.description()?.descriptionTemplate != null) {
			const isPreviousVersion: boolean = this.description().descriptionTemplate.versionStatus === DescriptionTemplateVersionStatus.Previous;
			if (isPreviousVersion === true) {
				if (this.description().status?.internalStatus === DescriptionStatusEnum.Draft) {
					this.openDeprecatedDescriptionTemplateDialog();
				} else {
					this.availableDescriptionTemplates.push(this.description().descriptionTemplate);
				}
			}
		}
	}

	private openDeprecatedDescriptionTemplateDialog(): void {
		const dialogRef = this.dialog.open(DeprecatedDescriptionTemplateDialog, {
			data: {
				label: this.description().descriptionTemplate.label
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(
			result => {
				if (result) {
					this.descriptionService.updateDescriptionTemplate({
						id: this.description().id,
						hash: this.description().hash
					})
						.subscribe(
							result => {
								this.refresh.emit();
							},
							error => console.error(error));
				} else {
					this.availableDescriptionTemplates.push(this.description().descriptionTemplate);
				}
			});
	}
}
