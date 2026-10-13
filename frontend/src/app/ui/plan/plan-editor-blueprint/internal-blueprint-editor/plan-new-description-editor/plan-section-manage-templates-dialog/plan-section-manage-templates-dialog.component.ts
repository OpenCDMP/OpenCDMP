import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { takeUntil } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { PlanSectionAddTemplatesDialogComponent } from '../plan-section-add-templates-dialog/plan-section-add-templates-dialog.component';
import { TranslatePipe } from '@ngx-translate/core';

export interface ManageTemplatesDialogData {
    section: RootSection;
    // Template groupIds currently linked to the section on this plan.
    linkedGroupIds: Guid[];
    // Template groupIds that already have descriptions using them — cannot be removed.
    templatesInUseGroupIds: Guid[];
}

@Component({
    selector: 'app-plan-section-manage-templates-dialog',
    templateUrl: './plan-section-manage-templates-dialog.component.html',
    styleUrl: './plan-section-manage-templates-dialog.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIconButton, MatIcon, MatTooltip, MatButton, PlanSectionAddTemplatesDialogComponent, TranslatePipe]
})
export class PlanSectionManageTemplatesDialogComponent extends BaseComponent {
    // Two views: the linked-templates list, and the catalog browser to add more.
    view: 'list' | 'add' = 'list';

    linkedTemplates: DescriptionTemplate[] = [];
    workingGroupIds: Guid[] = [];
    initialized = false;

    private blueprintGroupIds = new Set<string>();
    private inUseGroupIds = new Set<string>();

    constructor(
        @Inject(MAT_DIALOG_DATA) public data: ManageTemplatesDialogData,
        private dialogRef: MatDialogRef<PlanSectionManageTemplatesDialogComponent>,
        private descriptionTemplateService: DescriptionTemplateService
    ){
        super();
        this.workingGroupIds = [...(data?.linkedGroupIds ?? [])];
        this.blueprintGroupIds = new Set(
            (data?.section?.descriptionTemplates ?? [])
                .map(x => x.descriptionTemplate?.groupId?.toString())
                .filter(x => !!x)
        );
        this.inUseGroupIds = new Set((data?.templatesInUseGroupIds ?? []).map(x => x?.toString()));
        this.loadLinkedTemplates();
    }

    private loadLinkedTemplates(){
        if(!this.workingGroupIds?.length){
            this.linkedTemplates = [];
            this.initialized = true;
            return;
        }
        this.descriptionTemplateService.query(
            this.descriptionTemplateService.buildLookup({
                groupIds: this.workingGroupIds,
                isActive: [IsActive.Active],
                statuses: [DescriptionTemplateStatus.Finalized],
                versionStatuses: [DescriptionTemplateVersionStatus.Current],
                order: { items: [nameof<DescriptionTemplate>(x => x.label)] },
                project: {
                    fields: [
                        nameof<DescriptionTemplate>(x => x.id),
                        nameof<DescriptionTemplate>(x => x.groupId),
                        nameof<DescriptionTemplate>(x => x.label),
                        nameof<DescriptionTemplate>(x => x.type) + '.' + nameof<DescriptionTemplateType>(x => x.id),
                        nameof<DescriptionTemplate>(x => x.type) + '.' + nameof<DescriptionTemplateType>(x => x.name),
                    ]
                }
            })).pipe(takeUntil(this._destroyed))
            .subscribe((result) => {
                this.linkedTemplates = result?.items ?? [];
                this.initialized = true;
            });
    }

    isFromBlueprint(groupId: Guid): boolean {
        return this.blueprintGroupIds.has(groupId?.toString());
    }

    isInUse(groupId: Guid): boolean {
        return this.inUseGroupIds.has(groupId?.toString());
    }

    onTemplateAdded(template: DescriptionTemplate){
        if(template?.groupId && !this.workingGroupIds.some(g => g.toString() === template.groupId.toString())){
            this.workingGroupIds = [...this.workingGroupIds, template.groupId];
            this.linkedTemplates = [...this.linkedTemplates, template];
        }
        this.view = 'list';
    }

    remove(template: DescriptionTemplate){
        if(this.isInUse(template.groupId)){ return; }
        this.workingGroupIds = this.workingGroupIds.filter(g => g.toString() !== template.groupId?.toString());
        this.linkedTemplates = this.linkedTemplates.filter(x => x.groupId?.toString() !== template.groupId?.toString());
    }

    save(){
        this.dialogRef.close(this.workingGroupIds);
    }

    close(){
        this.dialogRef.close();
    }
}
