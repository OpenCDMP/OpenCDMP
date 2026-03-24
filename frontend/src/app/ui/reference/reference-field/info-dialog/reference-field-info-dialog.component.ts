import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from "@angular/material/dialog";
import { ExternalFetcherBaseSourceConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { ReferenceTypeFieldInSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { ReferenceType, ReferenceTypeDefinition, ReferenceTypeField } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs';
import { nameof } from 'ts-simple-nameof';

@Component({
    selector: 'app-reference-field-info-dialog',
    templateUrl: './reference-field-info-dialog.component.html',
    styleUrls: ['./reference-field-info-dialog.component.scss'],
    standalone: false
})

export class ReferenceFieldInfoDialogComponent extends BaseComponent implements OnInit{

  reference: Reference;
  label: string = null;
  showSelect: boolean = false;
  referenceType: ReferenceType;

  fieldCodes = [
    nameof<ExternalFetcherBaseSourceConfiguration>(x => x.key),
    nameof<ReferenceTypeFieldInSection>(x => x.referenceType)
  ]

  constructor(
    public dialogRef: MatDialogRef<ReferenceFieldInfoDialogComponent>,
    protected language: TranslateService,
		protected dialog: MatDialog,
    private referenceTypeService: ReferenceTypeService,
    @Inject(MAT_DIALOG_DATA) public data: any
    ) {
      super();
      this.reference = data.reference;
      if (this.reference?.definition?.fields?.length > 0) {
        this.reference.definition.fields = this.reference.definition.fields.filter(x => !this.fieldCodes.includes(x.code));
      }

      this.label = data.label;
      this.showSelect = data?.showSelect ?? false;
  }

  ngOnInit(): void {
  
      this.referenceTypeService.getSingle(this.data.referenceTypeId, this.lookupFields())
      .pipe(takeUntil(this._destroyed))
      .subscribe( //TODO HANDLE-ERRORS
        referenceType => {
          this.referenceType = referenceType;
        }
      );
    }

    private lookupFields(): string[] {
        return [
          nameof<ReferenceType>(x => x.id),
          nameof<ReferenceType>(x => x.name),
          nameof<ReferenceType>(x => x.code),
    
          [nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.code)].join('.'),
          [nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.label)].join('.'),
          [nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.dataType)].join('.'),
          [nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.required)].join('.')
        ]
    }

    getFieldLabel(code: string): string {
      return this.referenceType?.definition?.fields?.find( x => x.code === code)?.label ?? code;
    }

    select(): void {
		  this.dialogRef.close(true);
	  }

    closeDialog(event: MouseEvent): void {
      if (event != null) {
        event.stopPropagation();
        event.preventDefault();
      }
      this.dialogRef.close();
    }

    
}
    

