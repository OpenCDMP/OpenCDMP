import { ChangeDetectionStrategy, Component, Signal, computed, effect, inject } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { TenantConfigurationEditorModel as GuidedToursEditorModel } from '@app/ui/admin/tenant-configuration/models/guided-tours/guided-tours-editor.model';
import { GuidedToursEditorResolver } from '@app/ui/admin/tenant-configuration/models/guided-tours/guided-tours-editor.resolver';
import { TenantConfigurationEditorModel as TermsEditorModel } from '@app/ui/admin/tenant-configuration/models/terms-and-conditions/terms-and-conditions-editor.model';
import { TermsAndConditionsEditorResolver } from '@app/ui/admin/tenant-configuration/models/terms-and-conditions/terms-and-conditions-editor.resolver';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { Observable, of, startWith, switchMap } from 'rxjs';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSectionService } from '../../shared/tc-section.service';
import { TcStateTagComponent } from '../../shared/tc-state-tag.component';

/** Users & onboarding: the terms and conditions shown on first sign-in, and the guided page tours. */
@Component({
	selector: 'app-tc-onboarding',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, RichTextEditorModule, TcSaveBarComponent, TcStateTagComponent],
	providers: [TcSectionService],
	templateUrl: './tc-onboarding.component.html',
	styleUrl: './tc-onboarding.component.scss',
})
export class TcOnboardingComponent {
	protected readonly section = inject(TcSectionService);

	protected readonly terms = this.section.part(TenantConfigurationType.TermsAndConditions, TermsAndConditionsEditorResolver.lookupFields(), () => new TermsEditorModel());
	protected readonly tours = this.section.part(TenantConfigurationType.GuidedTours, GuidedToursEditorResolver.lookupFields(), () => new GuidedToursEditorModel());

	protected readonly termsGroup = computed(() => this.terms.form()?.get('termsAndConditions') as UntypedFormGroup);
	protected readonly content = computed(() => this.termsGroup()?.get('content') as UntypedFormControl);
	protected readonly version = computed(() => this.termsGroup()?.get('version') as UntypedFormControl);
	protected readonly toursEnabled = computed(() => this.tours.form()?.get('guidedTours')?.get('enabled') as UntypedFormControl);

	protected readonly contentValue = toSignal(this.valueOf(this.content), { initialValue: null as string | null });
	private readonly versionValue = toSignal(this.valueOf(this.version), { initialValue: null as number | null });

	/** The version users last accepted; null when this tenant has not published terms yet. */
	protected readonly savedVersion = computed(() => {
		const record = this.terms.record();
		return record?.id ? (record.termsAndConditions?.version ?? 1) : null;
	});
	protected readonly nextVersion = computed(() => (this.savedVersion() ?? 0) + 1);
	/** True once "Publish as a new version" was pressed and not yet saved or undone. */
	protected readonly publishing = computed(() => {
		const saved = this.savedVersion();
		return saved != null && this.versionValue() === saved + 1;
	});
	protected readonly termsEditable = computed(() => this.section.canEdit && !this.terms.inherited());

	constructor() {
		// The terms editor model does not pass "disabled" down to its nested group, so disable it here,
		// when the record is inherited or the user may not edit.
		effect(() => {
			const group = this.termsGroup();
			if (group && !this.termsEditable() && group.enabled) group.disable();
		});
	}

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	protected publishNewVersion(): void {
		const control = this.version();
		if (!control) return;
		control.markAsDirty();
		control.setValue(this.nextVersion());
	}

	protected undoPublish(): void {
		const control = this.version();
		const saved = this.savedVersion();
		if (!control || saved == null) return;
		// Nothing else edited: going back to the saved version leaves the terms untouched.
		if (this.content()?.pristine) this.terms.form()?.markAsPristine();
		control.setValue(saved);
	}

	private valueOf<T>(control: Signal<UntypedFormControl | undefined>): Observable<T | null> {
		return toObservable(control).pipe(
			switchMap(c => c ? (c.valueChanges.pipe(startWith(c.value)) as Observable<T>) : of(null)),
		);
	}
}
