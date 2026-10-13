import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { StructureEditorBase, StructureEditorItem } from '../structure-editor-base';
import { StructureProblem } from '../structure-problems';
import { NavigationBreadcrumbComponent } from '../../../misc/breadcrumb/navigation-breadcrumb.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';
import { DateTimeFormatPipe } from '../../../../core/pipes/date-time-format.pipe';
import { RelativeTimeFormatPipe } from '../../../../core/pipes/relative-time.pipe';
import { Bp2CountPipe } from '../count.pipe';
import { Bp2ProblemsInPipe } from '../problems-in.pipe';

/**
 * The frame both structure editors share: header with actions and versions, mode banners, the three tabs,
 * the readiness button and the problems panel. The host projects the active tab's body, and may add header chips
 * with the `se2HeaderChips` attribute.
 */
@Component({
    selector: 'app-se2-editor-shell',
    templateUrl: './editor-shell.component.html',
    styleUrls: ['./editor-shell.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NavigationBreadcrumbComponent, MatButton, MatTooltip, RouterLink, MatIconButton, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, TranslatePipe, DateTimeFormatPipe, RelativeTimeFormatPipe, Bp2CountPipe, Bp2ProblemsInPipe]
})
export class EditorShellComponent {
	/** Icon of the header tile when none is set. */
	@Input() defaultIcon = 'description';

	constructor(public e: StructureEditorBase<any, any>) { }

	/** The status tag's key, in the editor's own status vocabulary. */
	statusKey(finalized: boolean): string { return this.e.term(finalized ? 'TYPES.PLAN-BLUEPRINT-STATUS.FINALIZED' : 'TYPES.PLAN-BLUEPRINT-STATUS.DRAFT'); }

	/** The checks that pass, listed closed under the problems so the panel is the whole checklist. */
	get passed(): StructureProblem[] { return this.e.problems.filter(p => p.severity === 'ok'); }

	isOpen(v: StructureEditorItem): boolean { return v.id?.toString() === this.e.item?.id?.toString() && !this.e.isNewVersion; }
}
