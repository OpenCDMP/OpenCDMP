import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { EvaluatorConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { EvaluatorRestriction } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { CommonUiModule } from '@common/ui/common-ui.module';

/** Which of an evaluator's benchmarks and tests the tenant offers. */
@Component({
	selector: 'app-tc-evaluator-restrictions',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	template: `
		@let config = configuration();
		@if (!config) {
			<p class="tc-restrictions__hint">{{ 'TENANT-CONFIGURATION.INTEGRATIONS.RESTRICTIONS.UNAVAILABLE' | translate }}</p>
		} @else {
			<mat-slide-toggle [checked]="!restriction()" [disabled]="readOnly()" (change)="setAll($event.checked)">
				{{ 'TENANT-CONFIGURATION.INTEGRATIONS.RESTRICTIONS.ALL' | translate }}
			</mat-slide-toggle>
			@if (restriction(); as r) {
				<fieldset class="tc-restrictions__group">
					<legend>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.RESTRICTIONS.BENCHMARKS' | translate }}</legend>
					@for (item of config.availableBenchmarks ?? []; track item.id) {
						<mat-checkbox [checked]="r.benchmarkIds.includes(item.id)" [disabled]="readOnly()" (change)="toggle('benchmarkIds', item.id, $event.checked)">{{ item.label }}</mat-checkbox>
					} @empty {
						<p class="tc-restrictions__hint">{{ 'TENANT-CONFIGURATION.INTEGRATIONS.RESTRICTIONS.NO-BENCHMARKS' | translate }}</p>
					}
				</fieldset>
				<fieldset class="tc-restrictions__group">
					<legend>{{ 'TENANT-CONFIGURATION.INTEGRATIONS.RESTRICTIONS.TESTS' | translate }}</legend>
					@for (item of config.availableTests ?? []; track item.id) {
						<mat-checkbox [checked]="r.testIds.includes(item.id)" [disabled]="readOnly()" (change)="toggle('testIds', item.id, $event.checked)">{{ item.label }}</mat-checkbox>
					} @empty {
						<p class="tc-restrictions__hint">{{ 'TENANT-CONFIGURATION.INTEGRATIONS.RESTRICTIONS.NO-TESTS' | translate }}</p>
					}
				</fieldset>
			}
		}
	`,
	styles: [`
		:host { display: block; }
		.tc-restrictions__group { display: flex; flex-direction: column; margin: 16px 0 0; padding: 0; border: 0; }
		.tc-restrictions__group legend { margin-bottom: 4px; font-size: 13px; font-weight: 700; }
		.tc-restrictions__hint { margin: 4px 0 0; font-size: 12px; color: var(--mat-sys-on-surface-variant); }
	`],
})
export class TcEvaluatorRestrictionsComponent {
	readonly evaluatorId = input.required<string>();
	/** Every benchmark and test the plugin offers, not only those the tenant allows. */
	readonly configuration = input<EvaluatorConfiguration | null>(null);
	readonly restriction = input<EvaluatorRestriction | null>(null);
	readonly readOnly = input(false);
	readonly restrictionChange = output<EvaluatorRestriction | null>();

	protected setAll(all: boolean): void {
		const config = this.configuration();
		// limiting starts from everything ticked, so nothing disappears until it is unticked
		this.restrictionChange.emit(all ? null : {
			evaluatorId: this.evaluatorId(),
			benchmarkIds: (config?.availableBenchmarks ?? []).map(x => x.id),
			testIds: (config?.availableTests ?? []).map(x => x.id),
		});
	}

	protected toggle(key: 'benchmarkIds' | 'testIds', id: string, checked: boolean): void {
		const restriction = this.restriction();
		if (!restriction) return;
		const ids = (restriction[key] ?? []).filter(x => x !== id);
		this.restrictionChange.emit({ ...restriction, [key]: checked ? [...ids, id] : ids });
	}
}
