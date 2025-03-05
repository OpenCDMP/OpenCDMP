import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";

@Component({
    selector: 'app-deprecated-description-template-dialog',
    templateUrl: 'deprecated-description-template-dialog.component.html',
    styleUrls: ['deprecated-description-template-dialog.component.scss'],
    standalone: false
})
export class DeprecatedDescriptionTemplateDialog implements OnInit {
  constructor(
		public dialogRef: MatDialogRef<DeprecatedDescriptionTemplateDialog>,
		  @Inject(MAT_DIALOG_DATA) public data: any
    ) { }
  
  ngOnInit(): void {
  }

  cancel(): void {
    this.dialogRef.close();
  }

  update(): void {
    this.dialogRef.close(true);
  }
}