import { CDK_DRAG_CONFIG, CdkDrag, CdkDragDrop, CdkDropList, DragDropConfig, CdkDropListGroup } from '@angular/cdk/drag-drop';
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BlueprintStructureHelper } from '../../structure.helper';
import { StructureEditorBase } from '../../structure-editor-base';
import { objectsOf, optionSemanticsOf } from '../../semantic-objects';
import { keysInUse, KeysViewComponent } from '../keys-view/keys-view.component';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { NgTemplateOutlet } from '@angular/common';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatMenuTrigger, MatMenu, MatMenuContent, MatMenuItem } from '@angular/material/menu';
import { MatDivider } from '@angular/material/list';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

/** The outline: a real tree of sections and questions with reorder by drag within one level. */
@Component({
    selector: 'app-bp2-outline',
    templateUrl: './outline.component.html',
    styleUrls: ['./outline.component.scss'],
    // a little more travel before a drag starts, so empty drop zones are laid out before positions are cached
    providers: [{ provide: CDK_DRAG_CONFIG, useValue: { dragStartThreshold: 10, pointerDirectionChangeThreshold: 5 } as DragDropConfig }],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatTooltip, MatIcon, KeysViewComponent, CdkDropListGroup, NgTemplateOutlet, CdkDropList, CdkDrag, MatIconButton, MatMenuTrigger, MatButton, MatMenu, MatMenuContent, MatMenuItem, MatDivider, TranslatePipe, Bp2CountPipe]
})
export class Bp2OutlineComponent {
	readonly ToCEntryType = ToCEntryType;
	private collapsed = new Set<string>();
	/** The entry being dragged, and the list the pointer is over, so a list that cannot take it can say why. */
	dragged: ToCEntry = null;
	hoverParent: ToCEntry = null;
	hovering = false;
	/**
	 * Set on pointer-down, before the drag starts: empty sections then render their drop zone, and the drag
	 * library caches list positions after that layout change instead of before it.
	 */
	armed: ToCEntry = null;

	arm(entry: ToCEntry, event: PointerEvent): void {
		if (this.editor.readOnly || event.button !== 0) return;
		event.stopPropagation();
		const x0 = event.clientX, y0 = event.clientY;
		// arm only once the pointer really moves, so a plain click never shifts the rows under it
		const move = (e: PointerEvent) => { if (!this.armed && Math.hypot(e.clientX - x0, e.clientY - y0) > 3) this.armed = entry; };
		const release = () => { if (!this.dragged) this.armed = null; document.removeEventListener('pointermove', move, true); document.removeEventListener('pointerup', release, true); };
		document.addEventListener('pointermove', move, true);
		document.addEventListener('pointerup', release, true);
	}
	/** The innermost list under the pointer wins, so moving back out of a child list updates the reason. */
	onOver(parent: ToCEntry | null, event: PointerEvent): void {
		event.stopPropagation();
		if (!this.dragged) return;
		this.hoverParent = parent;
		this.hovering = true;
	}
	disarm(): void { this.dragged = null; this.armed = null; this.hovering = false; }

	/** Mapping pass: show only questions none of whose inputs carries a semantic key. */
	unmappedOnly = false;

	constructor(public editor: StructureEditorBase<any, any>) { }

	get entries(): ToCEntry[] { return this.editor.toCEntries ?? []; }

	isSelected(entry: ToCEntry): boolean {
		const selected = this.editor.selectedTocEntry;
		if (!selected) return false;
		if (selected.id === entry.id) return true;
		// a selected question highlights itself only; its section is shown open in the pane
		return false;
	}
	isCollapsed(entry: ToCEntry): boolean { return this.collapsed.has(entry.id); }
	toggle(entry: ToCEntry, event: Event): void { event.stopPropagation(); if (this.collapsed.has(entry.id)) this.collapsed.delete(entry.id); else this.collapsed.add(entry.id); }

	select(entry: ToCEntry): void {
		if (entry.type === ToCEntryType.FieldSet) {
			const path = BlueprintStructureHelper.pathTo(entry.id, this.entries) ?? [];
			const parent = path.length > 1 ? path[path.length - 2] : null;
			if (parent) this.editor.selectEntry(parent);
			this.editor.expandedQuestionId = entry.id;
			setTimeout(() => document.getElementById('bp2-q-' + entry.id)?.scrollIntoView({ behavior: 'smooth', block: 'center' }));
		} else {
			this.editor.selectEntry(entry);
		}
	}

	/**
	 * One distinct marker per preset this editor offers: the first letter of its name, or its first two when that
	 * letter is taken (Title and Tags read "T" and "Ta").
	 */
	private get presetLetters(): Map<SystemFieldType, string> {
		const key = this.editor.language.currentLang + '|' + [...this.editor.requiredPresets, ...this.editor.optionalPresets].join(',');
		if (this.letterCache?.key === key) return this.letterCache.map;
		const map = new Map<SystemFieldType, string>();
		const taken = new Set<string>();
		[...this.editor.requiredPresets, ...this.editor.optionalPresets].forEach(p => {
			const name = (this.editor.enumUtils.toSystemFieldTypeString(p) ?? '').trim();
			let mark = name.charAt(0).toUpperCase();
			if (taken.has(mark)) mark = name.charAt(0).toUpperCase() + name.charAt(1).toLowerCase();
			taken.add(mark);
			map.set(p, mark);
		});
		this.letterCache = { key, map };
		return map;
	}
	private letterCache: { key: string, map: Map<SystemFieldType, string> } = null;

	/** Letters for the preset fields a section holds; on the section presets land in, missing required ones show dashed. */
	glyphs(entry: ToCEntry): { letter: string, title: string, missing: boolean }[] {
		if (entry.type !== ToCEntryType.Section) return [];
		const letter = (p: SystemFieldType) => { const name = this.editor.enumUtils.toSystemFieldTypeString(p) ?? ''; return { letter: this.presetLetters.get(p) ?? name.charAt(0).toUpperCase(), name }; };
		const present = BlueprintStructureHelper.presetsIn(entry.form as any).map((p: SystemFieldType) => { const l = letter(p); return { letter: l.letter, title: l.name, missing: false }; });
		if (this.editor.readOnly || this.editor.presetTarget()?.id !== entry.id) return present;
		const missing = this.editor.missingRequiredPresets.map(p => { const l = letter(p); return { letter: l.letter, title: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.PRESETS.MISSING-GLYPH', { name: l.name }), missing: true }; });
		return [...present, ...missing];
	}
	hasTemplates(entry: ToCEntry): boolean { return entry.root && !!entry.form.get('hasTemplates')?.value; }
	hasSubsections(entry: ToCEntry): boolean { return BlueprintStructureHelper.hasSubsections(entry); }
	hasQuestions(entry: ToCEntry): boolean { return BlueprintStructureHelper.hasQuestions(entry); }
	questionCount(entry: ToCEntry): number { return BlueprintStructureHelper.countQuestions(entry); }
	subsectionCount(entry: ToCEntry): number { return entry.subEntries?.filter(e => e.type === ToCEntryType.Section).length ?? 0; }
	warn(entry: ToCEntry): boolean { return entry.form.invalid && (this.editor.attemptedSubmit || entry.form.touched || entry.form.dirty); }
	/** Any input of the question carries a semantic key. */
	/** A question with a key on an input or an answer, or one that describes an object. */
	isMapped(entry: ToCEntry): boolean {
		if (objectsOf(entry.form as UntypedFormGroup).length) return true;
		return ((entry.form.get('fields') as UntypedFormArray)?.controls ?? []).some(f => (f.get('semantics')?.value ?? []).length > 0 || optionSemanticsOf(f as UntypedFormGroup).length > 0);
	}
	/** The objects a section or question describes, as outline chips. */
	objectsOf(entry: ToCEntry): string[] { return objectsOf(entry.form as UntypedFormGroup); }
	objectName(key: string): string { return key.slice(key.lastIndexOf('.') + 1); }
	/** A question carrying a key with the prefix the keys-in-use card asked to highlight. */
	isHighlighted(entry: ToCEntry): boolean {
		const prefix = this.editor.semanticHighlight;
		if (!prefix || entry.type !== ToCEntryType.FieldSet) return false;
		const under = (k: string) => k === prefix || k.startsWith(prefix + '.');
		if (objectsOf(entry.form as UntypedFormGroup).some(under)) return true;
		return ((entry.form.get('fields') as UntypedFormArray)?.controls ?? []).some(f => ((f.get('semantics')?.value ?? []) as string[]).some(under) || optionSemanticsOf(f as UntypedFormGroup).some(x => under(x.key)));
	}
	controlsOthers(entry: ToCEntry): boolean { return entry.type === ToCEntryType.FieldSet && this.editor.rulesFrom(entry.form as any).some(l => !!l.targetId); }
	isConditional(entry: ToCEntry): boolean { return entry.type === ToCEntryType.FieldSet && this.editor.rulesInto(entry.form as any).length > 0; }
	hidden(entry: ToCEntry): boolean { return this.unmappedOnly && entry.type === ToCEntryType.FieldSet && (this.isMapped(entry) || this.isPresetQuestion(entry)); }
	get unmappedCount(): number {
		let n = 0;
		const visit = (list: ToCEntry[]) => list?.forEach(e => { if (e.type === ToCEntryType.FieldSet) { if (!this.isMapped(e) && !this.isPresetQuestion(e)) n++; } else visit(e.subEntries); });
		visit(this.entries);
		return n;
	}

	isPresetQuestion(entry: ToCEntry): boolean { return entry.type === ToCEntryType.FieldSet && BlueprintStructureHelper.isPresetQuestion(entry.form as any); }

	/** How many mappings of the question won't come out right, for the red dot; the Review tab lists them. */
	mappingProblems(entry: ToCEntry): number { return this.editor.checkProblemsFor(entry.id).length; }
	/** Keys in use, for the Keys switch. */
	get keyCount(): number { return keysInUse(this.entries).length; }

	/** Only lists that may hold the dragged entry accept it; the rest say why while the pointer is over them. */
	canEnter = (drag: CdkDrag<ToCEntry>, drop: CdkDropList<{ parent: ToCEntry, list: ToCEntry[] }>): boolean =>
		!this.editor.moveBlockReason(drag.data, drop.data.parent);

	blockReason(parent: ToCEntry | null): string { return this.dragged ? this.editor.moveBlockReason(this.dragged, parent) : null; }

	/** Within a list: reorder. Across lists: move under the new parent. Holding Alt copies instead. */
	drop(event: CdkDragDrop<{ parent: ToCEntry, list: ToCEntry[] }>): void {
		const entry: ToCEntry = event.item.data;
		const target = event.container.data.parent;
		const copy = !!(event.event as MouseEvent)?.altKey;
		this.disarm();
		if (event.previousContainer === event.container && !copy) {
			if (event.previousIndex === event.currentIndex) return;
			const array = this.siblingsArray(target, event.container.data.list);
			if (array) this.editor.reorder(array, event.previousIndex, event.currentIndex);
			return;
		}
		this.editor.moveEntryTo(entry, target, event.currentIndex, copy);
	}

	private siblingsArray(parent: ToCEntry | null, list: ToCEntry[]): UntypedFormArray {
		if (!parent) return this.editor.sectionsArray as unknown as UntypedFormArray;
		const isQuestions = list?.[0]?.type === ToCEntryType.FieldSet;
		return parent.form.get(isQuestions ? 'fieldSets' : 'sections') as UntypedFormArray;
	}

	canMove(entry: ToCEntry, direction: -1 | 1): boolean {
		const array = entry.form.parent as UntypedFormArray;
		const index = array?.controls.findIndex(c => c.get('id').value === entry.id) ?? -1;
		const target = index + direction;
		return index >= 0 && target >= 0 && target < array.length;
	}
}
