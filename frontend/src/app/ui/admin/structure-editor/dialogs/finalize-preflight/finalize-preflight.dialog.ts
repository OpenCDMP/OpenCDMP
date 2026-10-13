import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { StructureProblem } from '../../structure-problems';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';

export interface FinalizePreflightData {
	name: string;
	problems: StructureProblem[];
	version: number;
	tenantName: string;
	/** The intro line, in the editor's own wording. */
	introKey?: string;
}

/**
 * The Finalize button always opens this dialog. It shows the same list as the problems panel, so nothing here
 * is a surprise, and enables Finalize only when no blocker is left.
 */
@Component({
    selector: 'app-bp2-finalize-preflight-dialog',
    templateUrl: './finalize-preflight.dialog.html',
    styleUrls: ['./finalize-preflight.dialog.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatIcon, MatButton, MatDialogActions, TranslatePipe]
})
export class FinalizePreflightDialogComponent {
	constructor(public dialogRef: MatDialogRef<FinalizePreflightDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: FinalizePreflightData) { }

	get blockers(): StructureProblem[] { return this.data.problems.filter(p => p.severity === 'blocker'); }
	get oks(): StructureProblem[] { return this.data.problems.filter(p => p.severity === 'ok'); }
	get optionals(): StructureProblem[] { return this.data.problems.filter(p => p.severity === 'optional'); }

	fix(problem: StructureProblem): void { this.dialogRef.close({ problem }); }
	finalize(): void { this.dialogRef.close('finalize'); }
	cancel(): void { this.dialogRef.close(null); }
}
