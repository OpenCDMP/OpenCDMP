import { AsyncPipe, NgTemplateOutlet } from '@angular/common';
import { booleanAttribute, Component, computed, effect, Input, input, QueryList, ViewChild, ViewChildren, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Description, PropertyDefinitionField, PropertyDefinitionFieldSetItem, DescriptionReference } from '@app/core/model/description/description';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { MaterialModule } from '@common/material/material.module';
import { VisibilityRulesService } from '../../editor/description-form/visibility-rules/visibility-rules.service';
import { catchError, forkJoin, map, Observable, of, takeUntil, tap } from 'rxjs';
import { FieldValuePipe } from '@app/core/pipes/field-value.pipe';
import { BaseField, BaseFieldSet, BaseSection } from '@app/core/model/description-template/description-template';
import { BaseComponent } from '@common/base/base.component';
import { FieldType } from '@app/core/common/enum/field-type';
import { TranslateModule } from '@ngx-translate/core';
import { FormattingModule } from '@app/core/formatting.module';
import { Guid } from '@common/types/guid';
import { FileAsyncPipe } from '@app/core/pipes/file-async.pipe';
import { PlanAsyncPipe } from '@app/core/pipes/plan-async.pipe';
import { DescriptionAsyncPipe } from '@app/core/pipes/description-async.pipe';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { AuthService } from '@app/core/services/auth/auth.service';
import { MatAccordion, MatExpansionPanel } from '@angular/material/expansion';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { TagService } from '@app/core/services/tag/tag.service';
import { ReferenceCardComponent } from '@app/ui/reference/reference-card/reference-card.component';
@Component({
  selector: 'app-description-preview',
  imports: [TranslateModule, CommonFormattingModule, FormattingModule, MaterialModule, FileAsyncPipe, PlanAsyncPipe, DescriptionAsyncPipe, AsyncPipe, ReferenceCardComponent, NgTemplateOutlet],
  templateUrl: './description-preview.component.html',
  styleUrl: './description-preview.component.scss',
  // OnPush: the plan overview runs ~13 change detections per expand/collapse click, and each re-checked every preview.
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [VisibilityRulesService]
})
export class DescriptionPreviewComponent extends BaseComponent{
    @ViewChild('templateAccordion') accordion: MatAccordion;
    @ViewChildren('templateExpansionPanel') expansionPanels: QueryList<MatExpansionPanel>;
    @ViewChildren('templateSectionExpansionPanel') sectionExpansionPanels: QueryList<MatExpansionPanel>;
    @ViewChildren('templateSectionAccordion') sectionAccordions: QueryList<MatAccordion>;
    

    @Input({transform: booleanAttribute}) expanded?: boolean = false;
    
    description = input<Description>(null);

    descriptionTags = computed(() => this.description()?.descriptionTags?.map(x => x.tag?.label)?.join(', '))
    
    public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

    descriptionTemplateFieldType = FieldType;
    SystemFieldType = SystemFieldType;

    fieldValueMap: Map<Guid, PropertyDefinitionField>;

    constructor(
        protected visibilityRulesService: VisibilityRulesService,
        private fieldValuePipe: FieldValuePipe,
        private authentication: AuthService,
        private tagService: TagService,
        private sanitizer: DomSanitizer,
        private storageFileService: StorageFileService,
        private cdr: ChangeDetectorRef
    ) {
        super();
        effect(() => {
            if(this.description()){
                if (this.description().descriptionTemplate?.definition){
                    this.visibilityRulesService.setContext(this.description().descriptionTemplate.definition, null, this.description().properties);
                }
                this.description().descriptionTemplate?.definition?.sections?.forEach((section) => this.initSectionFieldSetValues(section))
                this.loadReferenceData();
            }
        })
    }

    referenceLogos: Map<Guid, SafeResourceUrl>;             // keyed by storageFileId (dedups downloads)
    referenceStorageFileByRefId: Map<Guid, Guid>;           // reference.id -> storageFileId
    referencesByFieldId: Map<string, DescriptionReference[]>; // template fieldId -> description references
    referenceTooltips: Map<Guid, string>;                   // reference.id -> tooltip
    private referenceObjectUrls: string[] = [];

    override ngOnDestroy(): void {
        this.referenceObjectUrls.forEach(u => URL.revokeObjectURL(u));
        super.ngOnDestroy();
    }

    private loadReferenceData(){
        this.referenceObjectUrls.forEach(u => URL.revokeObjectURL(u));
        this.referenceObjectUrls = [];
        this.referenceLogos = new Map<Guid, SafeResourceUrl>([]);
        this.referenceStorageFileByRefId = new Map<Guid, Guid>([]);
        this.referencesByFieldId = new Map<string, DescriptionReference[]>([]);
        this.referenceTooltips = new Map<Guid, string>([]);

        const refs = this.description()?.descriptionReferences;
        if(!refs?.length){ return; }
        const tempStorageFileIdSet = new Set([]);

        refs.forEach((ref) => {
            const fieldId = ref.data?.fieldId?.toString();
            if(fieldId){
                const list = this.referencesByFieldId.get(fieldId) ?? [];
                list.push(ref);
                this.referencesByFieldId.set(fieldId, list);
            }
            const sourceDefinition = ref.reference?.type?.definition?.sources?.find(x => x.key == ref.reference.source);
            if(!sourceDefinition){ return; }
            const referenceId = ref.reference?.id;
            this.referenceTooltips.set(referenceId, sourceDefinition?.tooltipMessage);
            const storageFileId = sourceDefinition?.storageFile?.id;
            if(storageFileId){
                this.referenceStorageFileByRefId.set(referenceId, storageFileId);
                if(!tempStorageFileIdSet.has(storageFileId)){
                    tempStorageFileIdSet.add(storageFileId);
                    this.storageFileService.download(storageFileId).pipe(takeUntil(this._destroyed))
                    .subscribe(response => {
                        const url = URL.createObjectURL(response.body);
                        this.referenceObjectUrls.push(url);
                        this.referenceLogos.set(storageFileId, this.sanitizer.bypassSecurityTrustUrl(url));
                        this.cdr.markForCheck();
                    });
                }
            }
        });
        this.referencesByFieldId.forEach((list) => list.sort((a, b) => (a.data?.ordinal ?? 0) - (b.data?.ordinal ?? 0)));
    }

    private readonly emptyRefs: DescriptionReference[] = [];
    getFieldReferences(fieldId: string): DescriptionReference[] {
        return this.referencesByFieldId?.get(fieldId?.toString()) ?? this.emptyRefs;
    }
    hasRenderableValue(values: string[]): boolean {
        return values?.some(v => v != null && v !== '') ?? false;
    }
    getReferenceLogo(referenceId: Guid): SafeResourceUrl {
        const storageFileId = this.referenceStorageFileByRefId?.get(referenceId);
        return storageFileId ? this.referenceLogos?.get(storageFileId) : null;
    }
    getReferenceTooltip(referenceId: Guid): string {
        return this.referenceTooltips?.get(referenceId);
    }

    // initFieldSetValues: (fieldSet: BaseFieldSet) => Observable<Map<string, string>[]>  = (fieldSet) => {
    //     const properties = this.description()?.properties.fieldSets?.[fieldSet.id]?.items ?? [];
    //     let fieldValueMapArray = [];

    //     if(!properties){ return of([])}
    //     const callStack: Observable<Map<string, string>>[] = properties.map((item) => this.buildFieldMap(fieldSet, item.fields));
    //     return forkJoin(
    //         callStack
    //     ).pipe(
    //         map((res) => {
    //             fieldValueMapArray = [];
    //             res?.forEach((itemMap) => fieldValueMapArray.push(itemMap));
    //             return fieldValueMapArray;
    //         }),
    //         catchError((error) => of([]))
    //     )

    // }
    // buildFieldMap(fieldSet: BaseFieldSet, fieldValues: Record<string, FieldPersist>): Observable<Map<string, string>>{
    //     const itemMap = new Map<string, string>([]);
    //     return forkJoin(
    //         Object.keys(fieldValues)?.map((key) => {
    //             const field = fieldSet.fields.find((x) => x.id === key);
    //             return this.fieldValuePipe.transform(fieldValues[key], field, this.description())
    //             .pipe(
    //                 takeUntil(this._destroyed), 
    //                 catchError(() => of(null)),
    //                 tap((res) => itemMap.set(key,res))
    //             )
    //         })
    //     ).pipe(switchMap(() => of(itemMap)), catchError((error) => of(new Map<string, string>([]))))
    // }

    fieldSetValueMap = new Map<string, Map<string, string[]>>([]);
    FieldType = FieldType;
    
    initSectionFieldSetValues(section: BaseSection)  {
        if(section.sections){
            section.sections.forEach((x) => this.initSectionFieldSetValues(x))
        }
        if(section.fieldSets){
            section.fieldSets.forEach((fieldSet) => {
                this.initFieldSetValues(fieldSet).pipe(takeUntil(this._destroyed))
                .subscribe((fieldsetMap) => { this.fieldSetValueMap.set(fieldSet.id, fieldsetMap); this.cdr.markForCheck(); })
            })
        }
    }

    initFieldSetValues: (fieldSet: BaseFieldSet) => Observable<Map<string, string[]>>  = (fieldSet) => {
        const properties: PropertyDefinitionFieldSetItem[]  = this.description()?.properties?.fieldSets?.[fieldSet.id]?.items ?? [];
        
        const fieldValueMapArray = new Map<string, string[]>([]);

        const callStack: Observable<string[]>[] = fieldSet.fields.map((field) =>
            this.buildFieldMap(field, properties).pipe(tap((res) => fieldValueMapArray.set(field.id, res)))
        )
        return forkJoin(
            callStack
        ).pipe(
            map((res) => {
                return fieldValueMapArray;
            }),
            catchError((error) => of(new Map<string, string[]>([])))
        )
    }

    buildFieldMap(field: BaseField, items: PropertyDefinitionFieldSetItem[]): Observable<string[]>{
        // Multi-item: keep values position-aligned to items (nulls preserved) so value[j] matches
        // properties.items[j] and its per-item visibility ordinal. Single-item: keep prior "hide empty".
        const fieldValues = (items?.length > 1
            ? items.map(x => x.fields?.[field.id] ?? null)
            : items?.map(x => x.fields?.[field.id])?.filter(x => !!x)) as PropertyDefinitionField[];
        if(fieldValues?.length){
            return forkJoin(
                fieldValues.map((value) => {
                    return this.fieldValuePipe.transform(value, field, this.description())
                    .pipe(
                        takeUntil(this._destroyed), 
                        catchError(() => of(null))
                    )
                })
            ).pipe(catchError((error) => of([])))
        }else if(field.data?.fieldType == FieldType.SYSTEM) {
            //check for system fields 
            return this.fieldValuePipe.transform(null, field, this.description()).pipe(takeUntil(this._destroyed), map((res) => [res]))
        }
        return of([])
    }

    getLabels(array: any[]): string {
        return array?.map((x) => x.label)?.join(', ');
    }

    get showExpand(): boolean {
        if(!this.expansionPanels?.length){ return !this.expanded }
        return (this.expansionPanels?.some(panel => !panel.expanded) || this.sectionExpansionPanels?.some(panel => !panel.expanded)) ?? false;
    }

    toggleExpand(){
        if(this.showExpand){
            this.accordion?.openAll();
            this.sectionAccordions?.forEach((acc) => acc.openAll());
        } else {
            this.accordion?.closeAll();
            this.sectionAccordions?.forEach((acc) => acc.closeAll());
        }
    }
}
