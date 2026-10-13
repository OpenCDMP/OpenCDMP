import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';
import { AbstractControl, ReactiveFormsModule, UntypedFormControl } from '@angular/forms';
import { CommonUiModule } from '@common/ui/common-ui.module';

/**
 * The connection details every plugin kind shares: where it runs and how the platform signs in to it.
 * Works on any group with `url`, `issuerUrl`, `clientId`, `clientSecret` and `scope` controls.
 */
@Component({
	selector: 'app-tc-connection-fields',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule],
	template: `
		<mat-form-field class="tc-conn__full">
			<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.URL' | translate }}</mat-label>
			<input matInput type="url" [formControl]="control('url')" required>
			<mat-hint>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.URL-HINT' | translate }}</mat-hint>
			<ng-container *ngTemplateOutlet="errors; context: { $implicit: control('url') }" />
		</mat-form-field>
		<fieldset class="tc-conn__auth">
			<legend>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.AUTH' | translate }}</legend>
			<mat-form-field class="tc-conn__full">
				<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.ISSUER' | translate }}</mat-label>
				<input matInput type="url" [formControl]="control('issuerUrl')" required>
				<ng-container *ngTemplateOutlet="errors; context: { $implicit: control('issuerUrl') }" />
			</mat-form-field>
			<div class="tc-conn__pair">
				<mat-form-field>
					<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.CLIENT-ID' | translate }}</mat-label>
					<input matInput [formControl]="control('clientId')" required autocomplete="off">
					<ng-container *ngTemplateOutlet="errors; context: { $implicit: control('clientId') }" />
				</mat-form-field>
				<mat-form-field>
					<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.CLIENT-SECRET' | translate }}</mat-label>
					<input matInput [type]="showSecret() ? 'text' : 'password'" [formControl]="control('clientSecret')" required autocomplete="new-password">
					<button mat-icon-button matSuffix type="button" (click)="showSecret.set(!showSecret())"
						[attr.aria-label]="(showSecret() ? 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.HIDE-SECRET' : 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.SHOW-SECRET') | translate"
						[attr.aria-pressed]="showSecret()">
						<mat-icon aria-hidden="true">{{ showSecret() ? 'visibility_off' : 'visibility' }}</mat-icon>
					</button>
					<ng-container *ngTemplateOutlet="errors; context: { $implicit: control('clientSecret') }" />
				</mat-form-field>
			</div>
			<mat-form-field class="tc-conn__full">
				<mat-label>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.CONNECTION.SCOPE' | translate }}</mat-label>
				<input matInput [formControl]="control('scope')" required>
				<ng-container *ngTemplateOutlet="errors; context: { $implicit: control('scope') }" />
			</mat-form-field>
		</fieldset>

		<ng-template #errors let-c>
			@if (c.hasError('required')) { <mat-error>{{ 'GENERAL.VALIDATION.REQUIRED' | translate }}</mat-error> }
			@if (c.hasError('backendError')) { <mat-error>{{ c.getError('backendError').message }}</mat-error> }
		</ng-template>
	`,
	styles: [`
		:host { display: block; }
		.tc-conn__full { width: 100%; }
		.tc-conn__pair { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
		.tc-conn__auth { margin: 8px 0 0; padding: 0; border: 0; }
		.tc-conn__auth legend { margin-bottom: 8px; font-size: 13px; font-weight: 700; }
		@media (max-width: 560px) { .tc-conn__pair { grid-template-columns: 1fr; } }
	`],
})
export class TcConnectionFieldsComponent {
	readonly group = input.required<AbstractControl>();
	protected readonly showSecret = signal(false);

	protected control(name: string): UntypedFormControl {
		return this.group().get(name) as UntypedFormControl;
	}
}
