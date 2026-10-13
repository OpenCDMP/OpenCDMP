import { Component, effect, input, output, ChangeDetectionStrategy } from '@angular/core';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { BaseFieldSet, BaseSection, DescriptionTemplate, DescriptionTemplateDefinition } from '@app/core/model/description-template/description-template';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseComponent } from '@common/base/base.component';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { catchError, takeUntil, throwError } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { TextFilterComponent } from '../../../../../../../common/modules/text-filter/text-filter.component';
import { NgTemplateOutlet, NgClass } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-section-add-templates-dialog',
    templateUrl: './plan-section-add-templates-dialog.component.html',
    styleUrl: './plan-section-add-templates-dialog.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIconButton, MatTooltip, MatIcon, TextFilterComponent, NgTemplateOutlet, MatButton, NgClass, TranslatePipe]
})
export class PlanSectionAddTemplatesDialogComponent extends BaseComponent{
    sectionTemplates = input<Guid[]>([]);
    onClose = output<void>();
    onTemplateSelected = output<DescriptionTemplate>();
    constructor(
        private descriptionTemplateService: DescriptionTemplateService,
        private uiNotificationService: UiNotificationService,
        private filterService: FilterService
    ){
        super();
        effect(() => {
            //unused sectionTemplates, to re-run every time the section templates input changes
            const sectionTemplates = this.sectionTemplates();
            this.getTemplates()
        })
    }

    selectedTemplate: DescriptionTemplate;

    templates: DescriptionTemplate[] = [];
    searchTerm: string;

    get searchTermUnmodified(): string {
        return this.filterService.reverseLikeTransformation(this.searchTerm);
    }

    get noTemplatesMessage(): { key: string, params?: Record<string, unknown> } {
        if (this.searchTerm) {
            return { key: 'NEW-DESCRIPTION.FROM-TEMPLATE.NO-TEMPLATES-MATCH-SEARCH', params: { term: this.searchTermUnmodified } };
        }
        return { key: 'NEW-DESCRIPTION.FROM-TEMPLATE.NO-TEMPLATES' };
    }

    offset: number = 0;
    count = 0;
    totalCount = 0;
    initialized: boolean = false;

    getTemplates(like?: string, offset?: number){
        this.offset = offset ?? 0;
        const lookup = new DescriptionTemplateLookup();

        this.descriptionTemplateService.query(
            this.descriptionTemplateService.buildLookup({
            ...lookup,
            like: like ?? null,
            excludedGroupIds: this.sectionTemplates()?.length ? this.sectionTemplates() : null,
            isActive: [IsActive.Active],
            statuses: [DescriptionTemplateStatus.Finalized],
            versionStatuses: [DescriptionTemplateVersionStatus.Current],
            page: {
                offset: this.offset ?? 0,
                size: 5
            },
            order: {
                items: [nameof<DescriptionTemplate>(x => x.label)]
            },
            project: {
                fields: [
                    nameof<DescriptionTemplate>(x => x.id),
                    nameof<DescriptionTemplate>(x => x.groupId),
                    nameof<DescriptionTemplate>(x => x.label),
                    nameof<DescriptionTemplate>(x => x.description),
                    nameof<DescriptionTemplate>(x => x.type) + '.' + nameof<DescriptionTemplateType>(x => x.id),
                    nameof<DescriptionTemplate>(x => x.type) + '.' + nameof<DescriptionTemplateType>(x => x.name),
                    nameof<DescriptionTemplate>(x => x.definition) + '.' + nameof<DescriptionTemplateDefinition>(x => x.sections),
                    nameof<DescriptionTemplate>(x => x.definition) + '.' + nameof<DescriptionTemplateDefinition>(x => x.sections) + '.' + nameof<BaseSection>(x => x.id),
                    nameof<DescriptionTemplate>(x => x.definition) + '.' + nameof<DescriptionTemplateDefinition>(x => x.sections) + '.' + nameof<BaseSection>(x => x.title),
                    nameof<DescriptionTemplate>(x => x.definition) + '.' + nameof<DescriptionTemplateDefinition>(x => x.sections) + '.' + nameof<BaseSection>(x => x.sections),
                    nameof<DescriptionTemplate>(x => x.definition) + '.' + nameof<DescriptionTemplateDefinition>(x => x.sections) + '.' + nameof<BaseSection>(x => x.fieldSets),
                    nameof<DescriptionTemplate>(x => x.definition) + '.' + nameof<DescriptionTemplateDefinition>(x => x.sections) + '.' + nameof<BaseSection>(x => x.fieldSets) + '.'  + nameof<BaseFieldSet>((x) => x.id),
                ]
            }
        })).pipe(
            takeUntil(this._destroyed),
            catchError((err) => {this.onCallbackError(err); return throwError(() => err)})
        )
        .subscribe((result) => {
            this.count = result.count;
            if(!like && !offset){
                this.totalCount = result.count;
            }
            //load more
            if(offset){
                this.templates = [...this.templates, ...(result?.items ?? [])];
            }else{
                this.templates = result?.items;
            }
            this.countFields(result?.items);
            this.initialized = true;
        })
    }


    loadMore(){
        this.getTemplates(this.searchTerm, this.offset + 5);
    }

    fieldCount = new Map<Guid, number>([]);
    countFields(templates: DescriptionTemplate[]){
        templates.forEach((template) => {
            let total = 0;
            template.definition?.sections?.forEach((section) => {
                total += this.countSectionFields(section);
            })
            this.fieldCount.set(template.groupId, total);
        })
    }
    countSectionFields(section: BaseSection): number {
        let count = 0;
        if(section.sections?.length){
            section.sections?.forEach((section) => count += this.countSectionFields(section))
        }
        if(section.fieldSets?.length){
            count += section.fieldSets.length
        }
        return count;
    }

    onCallbackError(error: any) {
        this.uiNotificationService.snackBarNotification(error, SnackBarNotificationLevel.Error);
    }
}
