import { afterNextRender, ChangeDetectionStrategy, Component, effect, ElementRef, input, output, viewChild } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { TranslateModule } from '@ngx-translate/core';

export interface NameStepBanner {
	/** i18n key for the banner title, e.g. "Imported from {{source}}". */
	titleKey: string;
	titleParams?: Record<string, unknown>;
	subtitle?: string;
}

export interface NameStepResult {
	label: string;
	description: string;
}

/**
 * Last wizard step: name (required) and optional description for the asset about to be created.
 * Values arrive pre-filled when the asset was imported or copied; a banner says where from.
 */
@Component({
	selector: 'app-new-description-name-step',
	imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, RichTextEditorModule, TranslateModule],
	templateUrl: './new-description-name-step.component.html',
	styleUrl: './new-description-name-step.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewDescriptionNameStepComponent {
	label = input<string>('');
	description = input<string>('');
	banner = input<NameStepBanner | null>(null);
	busy = input<boolean>(false);

	submitted = output<NameStepResult>();

	private readonly nameInput = viewChild<ElementRef<HTMLInputElement>>('nameInput');

	protected readonly form = new FormGroup({
		label: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, noWhitespaceValidator] }),
		description: new FormControl<string>('', { nonNullable: true }),
	});

	constructor() {
		effect(() => {
			this.form.reset({ label: this.label() ?? '', description: this.description() ?? '' });
		});
		afterNextRender(() => this.nameInput()?.nativeElement.focus());
	}

	protected get canSubmit(): boolean {
		return this.form.valid && !this.busy();
	}

	protected submit(): void {
		if (!this.canSubmit) {
			this.form.markAllAsTouched();
			return;
		}
		const value = this.form.getRawValue();
		this.submitted.emit({ label: value.label.trim(), description: value.description ?? '' });
	}
}

function noWhitespaceValidator(control: AbstractControl<string>): ValidationErrors | null {
	return (control.value ?? '').trim().length ? null : { required: true };
}
