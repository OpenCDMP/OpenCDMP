import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ExternalFetcherBaseSourceConfigurationPersist, ResultFieldsMappingConfigurationPersist } from '@app/core/model/external-fetcher/external-fetcher';
import { Reference } from '@app/core/model/reference/reference';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { ReferenceFieldComponent } from '../../../reference/reference-field/reference-field.component';

@Component({
    selector: 'app-reference-type-test-dialog',
    templateUrl: './reference-type-test-dialog.component.html',
    styleUrls: ['./reference-type-test-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [ReferenceFieldComponent, TranslatePipe]
})

export class ReferenceTypeTestDialogComponent {

    sources: ExternalFetcherBaseSourceConfigurationPersist[] = [];
    key: string
    label: string
    referenceTypeId: Guid
    referenceSelected: Reference 
    

  constructor(
    public dialogRef: MatDialogRef<ReferenceTypeTestDialogComponent>,
    protected language: TranslateService,
		protected dialog: MatDialog,

    @Inject(MAT_DIALOG_DATA) public data: any
    ) {  
      this.sources = data.sources;    
      this.key = data.key;
      this.label = data.label;
      this.referenceTypeId = data.referenceTypeId;
    } 

    selectedOption(selectedOption: Reference){
      this.referenceSelected = selectedOption;
    }

    resultsValue(code: string): string {
      if (code === 'reference_id') return this.referenceSelected?.reference;
      const definitionValue = this.referenceSelected.definition?.fields?.filter((x) => x.code === code);
      return definitionValue?.length ? definitionValue.map((x) => x.value + ' ')?.toString() :  this.referenceSelected[code];
    }

    get fieldMappings(): ResultFieldsMappingConfigurationPersist[] {
      return this.sources?.[0]?.results?.fieldsMapping;
    }
}
    

