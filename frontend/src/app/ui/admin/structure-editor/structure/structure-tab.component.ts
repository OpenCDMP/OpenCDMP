import { Component, ChangeDetectionStrategy } from '@angular/core';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { StructureEditorBase } from '../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton, MatIconButton } from '@angular/material/button';
import { Bp2OutlineComponent } from './outline/outline.component';
import { Bp2SectionPaneComponent } from './section-pane/section-pane.component';
import { TranslatePipe } from '@ngx-translate/core';

/** Structure tab: the preset bar, the outline and the pane for the selected section. */
@Component({
    selector: 'app-bp2-structure-tab',
    templateUrl: './structure-tab.component.html',
    styleUrls: ['./structure-tab.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, MatTooltip, MatButton, MatIconButton, Bp2OutlineComponent, Bp2SectionPaneComponent, TranslatePipe]
})
export class Bp2StructureTabComponent {
	get requiredPresets(): SystemFieldType[] { return this.editor.requiredPresets; }
	get optionalPresets(): SystemFieldType[] { return this.editor.optionalPresets; }

	constructor(public editor: StructureEditorBase<any, any>) { }

	isPresent(preset: SystemFieldType): boolean { return this.editor.presentPresets.includes(preset); }
	get missing(): SystemFieldType[] { return this.editor.missingRequiredPresets; }
	get optionalMissing(): SystemFieldType[] { return this.optionalPresets.filter(p => !this.isPresent(p)); }
	get targetName(): string {
		const target = this.editor.presetTarget();
		return target?.label || this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.PRESETS.BASICS');
	}
}
