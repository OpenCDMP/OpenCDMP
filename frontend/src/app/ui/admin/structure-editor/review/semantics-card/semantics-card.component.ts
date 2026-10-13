import { Component, ChangeDetectionStrategy } from '@angular/core';
import { StructureEditorBase } from '../../structure-editor-base';
import { keysInUse, unmappedQuestions } from '../../structure/keys-view/keys-view.component';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

/**
 * One line on the Review tab: how many keys the structure uses and how many questions carry none. The full list,
 * grouped by object, is the Keys view of the Structure tab's left panel.
 */
@Component({
    selector: 'app-se2-semantics-card',
    templateUrl: './semantics-card.component.html',
    styleUrls: ['./semantics-card.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [TranslatePipe, Bp2CountPipe]
})
export class SemanticsCardComponent {
	constructor(public editor: StructureEditorBase<any, any>) { }

	get keyCount(): number { return keysInUse(this.editor.toCEntries).length; }
	get mappedInputs(): number { return this.editor.allFields().filter(f => ((f.get('semantics')?.value ?? []) as string[]).length > 0).length; }
	get unmappedCount(): number { return unmappedQuestions(this.editor.toCEntries).length; }
}
