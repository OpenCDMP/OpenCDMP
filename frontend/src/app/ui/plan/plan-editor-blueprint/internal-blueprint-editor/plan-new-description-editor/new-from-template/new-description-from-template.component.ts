import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';
import { countTemplateFieldSets } from '../shared/description-template-stats';
import { TemplateTypeIconComponent } from '../shared/template-type-icon.component';
import { templateTypeStyle } from '../shared/template-type-style';

/**
 * "Choose a template" step: the templates linked to the plan section, each with an Overview
 * (side sheet) and a direct "Use this" action. Purely presentational — the host loads the data.
 * Template curation lives in the section's "Manage templates" surface, not here.
 */
@Component({
	selector: 'app-new-description-from-template',
	imports: [MatButtonModule, MatIconModule, MatProgressBarModule, MatTooltipModule, TranslateModule, TemplateTypeIconComponent],
	templateUrl: './new-description-from-template.component.html',
	styleUrl: './new-description-from-template.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewDescriptionFromTemplateComponent {
	templates = input<DescriptionTemplate[]>([]);
	loading = input<boolean>(false);
	/** Template groups that already reached their maximum number of assets in this section. */
	maxMultiplicityIds = input<Guid[]>([]);
	/** Whether the current user may attach templates to the section (drives the empty-state copy). */
	canEditTemplateList = input<boolean>(false);

	useTemplate = output<DescriptionTemplate>();
	previewTemplate = output<DescriptionTemplate>();
	navigateToParentSection = output<void>();

	protected readonly fieldCount = countTemplateFieldSets;
	protected readonly typeStyle = templateTypeStyle;

	protected isAtMaxMultiplicity(template: DescriptionTemplate): boolean {
		return this.maxMultiplicityIds()?.includes(template.groupId) ?? false;
	}
}
