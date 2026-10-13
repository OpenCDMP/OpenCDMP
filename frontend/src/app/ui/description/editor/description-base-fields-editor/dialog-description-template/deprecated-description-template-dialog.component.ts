import { Component, Inject, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from "@angular/material/dialog";
import { MatIconButton, MatButton } from "@angular/material/button";
import { MatIcon } from "@angular/material/icon";
import { CdkScrollable } from "@angular/cdk/scrolling";
import { TranslatePipe } from "@ngx-translate/core";

@Component({
    selector: 'app-deprecated-description-template-dialog',
    templateUrl: 'deprecated-description-template-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatIconButton, MatIcon, CdkScrollable, MatDialogContent, MatDialogActions, MatButton, TranslatePipe]
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