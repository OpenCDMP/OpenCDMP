import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ExternalFetcherBaseSourceConfigurationPersist, ResultFieldsMappingConfigurationPersist } from '@app/core/model/external-fetcher/external-fetcher';
import { Prefilling } from '@app/core/model/prefilling-source/prefilling-source';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'prefilling-source-test-dialog',
    templateUrl: './prefilling-source-test-dialog.component.html',
    styleUrls: ['./prefilling-source-test-dialog.component.scss'],
    standalone: false
})

export class PrefillingSourceTestDialogComponent {

    sources: ExternalFetcherBaseSourceConfigurationPersist[] = [];
    key: string
    prefillingSelected: Prefilling 
    

  constructor(
    public dialogRef: MatDialogRef<PrefillingSourceTestDialogComponent>,
    protected language: TranslateService,
		protected dialog: MatDialog,

    @Inject(MAT_DIALOG_DATA) public data: any
    ) {  
      this.sources = data.sources;    
      this.key = data.key;
    } 

    selectedOption(selectedOption: Prefilling){
      this.prefillingSelected = selectedOption;
    }

    resultsValue(code: string): string {
      const definitionValue = this.prefillingSelected?.data[code]
      return definitionValue?.length ?  definitionValue  :  this.prefillingSelected[code];
    }

    get fieldMappings(): ResultFieldsMappingConfigurationPersist[] {
      return this.sources?.[0]?.results?.fieldsMapping;
    }
}
    

