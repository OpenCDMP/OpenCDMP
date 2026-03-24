import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, effect, input } from '@angular/core';
import { Description, DescriptionField, DescriptionFieldPersist } from '@app/core/model/description/description';
import { MaterialModule } from '@common/material/material.module';
import { VisibilityRulesService } from '../../editor/description-form/visibility-rules/visibility-rules.service';
import { catchError, forkJoin, map, Observable, of, switchMap, takeUntil, tap } from 'rxjs';
import { FieldValuePipe } from '@app/core/pipes/field-value.pipe';
import { DescriptionTemplateFieldSet } from '@app/core/model/description-template/description-template';
import { BaseComponent } from '@common/base/base.component';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { TranslateModule } from '@ngx-translate/core';
import { FormattingModule } from '@app/core/formatting.module';
import { Guid } from '@common/types/guid';
import { FileAsyncPipe } from '@app/core/pipes/file-async.pipe';
import { PlanAsyncPipe } from '@app/core/pipes/plan-async.pipe';
import { DescriptionAsyncPipe } from '@app/core/pipes/description-async.pipe';
import { CommonFormattingModule } from '@common/formatting/common-formatting.module';
import { ToggleShowTextComponent } from '@app/library/toggle-show-text/toggle-show-text.component';
@Component({
  selector: 'app-description-preview',
  imports: [CommonModule, TranslateModule, CommonFormattingModule, FormattingModule, MaterialModule, FileAsyncPipe, PlanAsyncPipe, DescriptionAsyncPipe, AsyncPipe, ToggleShowTextComponent],
  templateUrl: './description-preview.component.html',
  styleUrl: './description-preview.component.scss'
})
export class DescriptionPreviewComponent extends BaseComponent{
    description = input<Description>(null);
    isAuthenticated = input<boolean>(true);

    descriptionTemplateFieldType = DescriptionTemplateFieldType;

    fieldValueMap: Map<Guid, DescriptionField>;

    constructor(
        private visibilityRulesService: VisibilityRulesService,
        private fieldValuePipe: FieldValuePipe,
    ) {
        super();
        effect(() => {
            if(this.description()){
                if (this.description().descriptionTemplate?.definition){
                    this.visibilityRulesService.setContext(this.description().descriptionTemplate.definition, null, this.description().properties);
                }
            }
        })
    }

    initFieldSetValues: (fieldSet: DescriptionTemplateFieldSet) => Observable<Map<string, string>[]>  = (fieldSet) => {
        const properties = this.description()?.properties.fieldSets?.[fieldSet.id]?.items ?? [];
        let fieldValueMapArray = [];

        if(!properties){ return of([])}
        const callStack: Observable<Map<string, string>>[] = properties.map((item) => this.buildFieldMap(fieldSet, item.fields));
        return forkJoin(
            callStack
        ).pipe(
            map((res) => {
                fieldValueMapArray = [];
                res?.forEach((itemMap) => fieldValueMapArray.push(itemMap));
                return fieldValueMapArray;
            }),
            catchError((error) => of([]))
        )

    }
    buildFieldMap(fieldSet: DescriptionTemplateFieldSet, fieldValues: Record<string, DescriptionFieldPersist>): Observable<Map<string, string>>{
        const itemMap = new Map<string, string>([]);
        return forkJoin(
            Object.keys(fieldValues)?.map((key) => {
                const field = fieldSet.fields.find((x) => x.id === key);
                return this.fieldValuePipe.transform(fieldValues[key], field)
                .pipe(
                    takeUntil(this._destroyed), 
                    catchError(() => of(null)),
                    tap((res) => itemMap.set(key,res))
                )
            })
        ).pipe(switchMap(() => of(itemMap)), catchError((error) => of(new Map<string, string>([]))))
    }

    getLabels(array: any[]): string {
        return array?.map((x) => x.label)?.join(', ');
    }
}
