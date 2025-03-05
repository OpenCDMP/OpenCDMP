import { Component,Inject, OnDestroy, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ConfigurationService } from "@app/core/services/configuration/configuration.service";
import { Subscription } from "rxjs";

@Component({
    selector: 'merge-email-loader',
    templateUrl: './merge-email-loader-dialog.component.html',
    styleUrls: ['./merge-email-loader-dialog.component.scss'],
    standalone: false
})
export class MergeEmailLoaderDialogComponent implements OnInit, OnDestroy {
   

  confirmMergeAccountSubscription: Subscription;
  mergeAccountDelay: number = 60000;
  get mergeAccountDelayInSeconds(): number {
    return this.mergeAccountDelay/1000;
  }

  constructor(
    private dialogRef: MatDialogRef<MergeEmailLoaderDialogComponent>,
    private configurationService: ConfigurationService,
    @Inject(MAT_DIALOG_DATA) public data: any,

  ) {}
  

  ngOnInit(): void {
    if (this.configurationService.mergeAccountDelayInSeconds) this.mergeAccountDelay = this.configurationService.mergeAccountDelayInSeconds*1000;

    if (this.data.confirmMergeAccountObservable) {
      this.confirmMergeAccountSubscription = this.data.confirmMergeAccountObservable.subscribe(result => {
        if (result) {
          setTimeout( _ => this.onClose(true), this.mergeAccountDelay)
        }
      },
      error => this.onClose(false, error));
    }
  }
  
  onClose(success: boolean, error: any = null) {
    this.dialogRef.close({ result: success, error: error});
  }

  ngOnDestroy(): void {
    this.confirmMergeAccountSubscription?.unsubscribe();
  }

}