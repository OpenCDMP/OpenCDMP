import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle } from '@angular/material/dialog';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { DashboardSlideButtonType } from '@app/core/common/enum/dashboard-slide-button-type';
import { TranslatePipe } from '@ngx-translate/core';

export interface DashboardSlideDialogData {
	heading: string;
	position: string;
	title: string;
	text: string;
	buttonType: DashboardSlideButtonType | null;
	buttonTarget: string;
	buttonText: string;
}

/** The whole of a content slide whose text the Dashboard cuts, opened with "Read more". */
@Component({
	selector: 'app-dashboard-slide-dialog',
	templateUrl: './dashboard-slide-dialog.component.html',
	styleUrl: './dashboard-slide-dialog.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButton, MatIconButton, MatIcon, TranslatePipe]
})
export class DashboardSlideDialogComponent {
	protected readonly data = inject<DashboardSlideDialogData>(MAT_DIALOG_DATA);

	protected readonly isEmail = this.data.buttonType === DashboardSlideButtonType.Email;
	protected readonly href = this.isEmail ? `mailto:${this.data.buttonTarget}` : this.data.buttonTarget;
	protected readonly showButton = this.data.buttonType != null && !!this.data.buttonTarget && !!this.data.buttonText;
}
