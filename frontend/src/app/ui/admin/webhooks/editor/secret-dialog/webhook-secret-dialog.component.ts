import { Clipboard } from '@angular/cdk/clipboard';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';

export interface WebhookSecretDialogData {
	secret: string;
}

/**
 * Shows a freshly created subscription's signing secret. This is the only moment
 * the secret is ever visible — the API never returns it again.
 */
@Component({
	selector: 'app-webhook-secret-dialog',
	standalone: true,
	imports: [MatDialogModule, MatButtonModule, MatIconModule, MatTooltipModule, TranslateModule],
	templateUrl: './webhook-secret-dialog.component.html',
	styleUrl: './webhook-secret-dialog.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class WebhookSecretDialogComponent {

	private readonly dialogRef = inject(MatDialogRef<WebhookSecretDialogComponent>);
	private readonly clipboard = inject(Clipboard);
	protected readonly data = inject<WebhookSecretDialogData>(MAT_DIALOG_DATA);

	protected readonly copied = signal(false);

	protected copy(): void {
		this.copied.set(this.clipboard.copy(this.data.secret));
	}

	protected close(): void {
		this.dialogRef.close();
	}
}
