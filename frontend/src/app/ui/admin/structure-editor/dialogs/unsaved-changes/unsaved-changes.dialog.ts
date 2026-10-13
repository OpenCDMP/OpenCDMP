import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent } from '@angular/material/dialog';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

export interface UnsavedChangesData {
	/** Questions whose form changed since the last save. */
	questions: number;
	/** Questions that did not exist at the last save. */
	questionsAdded: number;
	/** Sections that did not exist at the last save. */
	sections: number;
	/** Name, code, consent, icon or export settings changed. */
	details: boolean;
	/** Save is not possible (a blocker is open); the dialog then offers only discard and keep editing. */
	canSave: boolean;
}

export type UnsavedChangesResult = 'save' | 'discard' | null;

/** Close with unsaved work: say what changed and offer the three honest ways out. */
@Component({
    selector: 'app-bp2-unsaved-changes-dialog',
    templateUrl: './unsaved-changes.dialog.html',
    styleUrls: ['./unsaved-changes.dialog.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatIcon, MatButton, TranslatePipe, Bp2CountPipe]
})
export class UnsavedChangesDialogComponent {
	constructor(public dialogRef: MatDialogRef<UnsavedChangesDialogComponent, UnsavedChangesResult>, @Inject(MAT_DIALOG_DATA) public data: UnsavedChangesData) { }

	close(result: UnsavedChangesResult): void { this.dialogRef.close(result); }
}
