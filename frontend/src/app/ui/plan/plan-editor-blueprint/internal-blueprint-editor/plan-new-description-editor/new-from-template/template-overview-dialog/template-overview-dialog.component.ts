import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { TranslateModule } from '@ngx-translate/core';
import { countTemplateFieldSets, countTemplateSections } from '../../shared/description-template-stats';
import { TemplateTypeIconComponent } from '../../shared/template-type-icon.component';
import { templateTypeStyle } from '../../shared/template-type-style';

export interface TemplateOverviewDialogData {
	template: DescriptionTemplate;
	/** True when the section already holds the maximum number of assets for this template. */
	disabled?: boolean;
	/** i18n key explaining why "Use this template" is unavailable. */
	disabledReason?: string;
}

export type TemplateOverviewDialogResult = 'use' | undefined;

/**
 * Right-hand side sheet previewing an asset template (stats, description, section structure)
 * before the user commits to it. Opened from the "Overview" action of the template step.
 */
@Component({
	selector: 'app-template-overview-dialog',
	imports: [NgTemplateOutlet, MatButtonModule, MatIconModule, MatTooltipModule, TranslateModule, TemplateTypeIconComponent],
	templateUrl: './template-overview-dialog.component.html',
	styleUrl: './template-overview-dialog.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TemplateOverviewDialogComponent {
	protected readonly data = inject<TemplateOverviewDialogData>(MAT_DIALOG_DATA);
	private readonly dialogRef = inject<MatDialogRef<TemplateOverviewDialogComponent, TemplateOverviewDialogResult>>(MatDialogRef);

	protected readonly template = this.data.template;
	protected readonly typeStyle = templateTypeStyle(this.template.type);
	protected readonly sectionCount = countTemplateSections(this.template);
	protected readonly fieldCount = countTemplateFieldSets(this.template);

	protected use(): void {
		this.dialogRef.close('use');
	}

	protected close(): void {
		this.dialogRef.close();
	}
}
