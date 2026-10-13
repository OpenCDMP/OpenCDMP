import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent } from '@angular/material/dialog';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { Subscription } from 'rxjs';
import { debounceTime, startWith, switchMap } from 'rxjs/operators';
import { SourceNode, StructureTransfer, TransferItem, TransferPayload } from '../../structure-transfer';
import { findStructures, loadStructure } from '../insert-from/insert-from.dialog';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatIcon } from '@angular/material/icon';
import { MatFormField, MatLabel, MatPrefix, MatHint } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

export interface MoveToData {
	entry: ToCEntry;
	entries: ToCEntry[];
	currentParentId: string;
	/** Why the entry cannot go under a target (null = top level), or null when it can. */
	reason: (target: ToCEntry | null) => string;
	/** The item being edited, left out of the other destinations. */
	currentId?: Guid;
	/** Why a place in another structure cannot take the entry, for its section rows. */
	holdsSubsectionsReason?: string;
	holdsQuestionsReason?: string;
	/** The "here" choice, in the editor's wording ("This template", "This blueprint"). */
	hereKey?: string;
	/** This editor has unsaved changes, which are saved before the other draft opens. */
	unsavedChanges?: boolean;
}

/** 'top' for the top level, otherwise the target section's id; `external` when the copy goes to another draft. */
export type MoveToResult = { targetId: string | 'top', copy: boolean, external?: TransferItem } | null;

interface Row { entry: ToCEntry | null, depth: number, reason: string, here: boolean }
interface PlaceRow { id: string | 'top', label: string, depth: number, reason: string }

/**
 * Where to move or copy a section or question: the section tree here, with the reason beside every place it cannot
 * go, or another draft template or blueprint, which then opens with the copy in it.
 */
@Component({
    selector: 'app-bp2-move-to-dialog',
    templateUrl: './move-to.dialog.html',
    styleUrls: ['./move-to.dialog.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatButtonToggleGroup, MatButtonToggle, MatIcon, MatFormField, MatLabel, MatPrefix, ReactiveAsteriskDirective, MatInput, FormsModule, ReactiveFormsModule, MatHint, MatButton, TranslatePipe, Bp2CountPipe]
})
export class MoveToDialogComponent {
	rows: Row[] = [];
	selected: string = null;

	mode: 'here' | 'elsewhere' = 'here';
	search = new FormControl('');
	destinations: TransferItem[] = null;
	destination: TransferItem = null;
	places: PlaceRow[] = null;
	place: string = null;
	private searching: Subscription = null;

	constructor(
		public dialogRef: MatDialogRef<MoveToDialogComponent, MoveToResult>,
		@Inject(MAT_DIALOG_DATA) public data: MoveToData,
		private templates: DescriptionTemplateService,
		private blueprints: PlanBlueprintService,
		private httpErrorHandlingService: HttpErrorHandlingService,
	) {
		const isSection = data.entry.type === ToCEntryType.Section;
		if (isSection) this.rows.push({ entry: null, depth: 0, reason: data.reason(null), here: data.currentParentId === null });
		const visit = (list: ToCEntry[], depth: number) => list?.forEach(e => {
			if (e.type !== ToCEntryType.Section) return;
			this.rows.push({ entry: e, depth: isSection ? depth + 1 : depth, reason: data.reason(e), here: e.id === data.currentParentId });
			visit(e.subEntries, depth + 1);
		});
		visit(data.entries, 0);
	}

	get isSection(): boolean { return this.data.entry.type === ToCEntryType.Section; }
	key(row: Row): string { return row.entry ? row.entry.id : 'top'; }
	canPick(row: Row): boolean { return !row.reason && !row.here; }
	pick(row: Row): void { if (this.canPick(row)) this.selected = this.key(row); }
	confirm(copy: boolean): void { if (this.selected) this.dialogRef.close({ targetId: this.selected, copy }); }

	// ---- another template or blueprint

	setMode(mode: 'here' | 'elsewhere'): void {
		this.mode = mode;
		if (mode === 'elsewhere' && !this.searching) {
			this.searching = this.search.valueChanges.pipe(startWith(''), debounceTime(250), switchMap(like => findStructures(this.templates, this.blueprints, like?.trim(), true, this.data.currentId)))
				.subscribe(items => this.destinations = items);
		}
	}

	pickDestination(item: TransferItem): void {
		this.destination = item;
		this.places = null;
		this.place = null;
		loadStructure(this.templates, this.blueprints, item).subscribe({
			next: tree => this.places = this.placesIn(tree),
			error: error => { this.destination = null; this.httpErrorHandlingService.handleBackedRequestError(error); }
		});
	}
	otherDestination(): void { this.destination = null; this.places = null; this.place = null; }

	/** A section lands after a top-level section or at the end; a question lands at the end of a section that holds questions. */
	private placesIn(tree: SourceNode[]): PlaceRow[] {
		const title = (n: SourceNode) => `${n.numbering} ${n.title}`;
		if (this.isSection) return [{ id: 'top', label: null, depth: 0, reason: null }, ...tree.map(n => ({ id: n.id, label: title(n), depth: 0, reason: null }))];
		return StructureTransfer.flatten(tree).filter(n => n.kind === 'section').map(n => {
			const depth = n.numbering.split('.').length - 1;
			const holdsSubsections = n.children.some(c => c.kind === 'section');
			return { id: n.id, label: title(n), depth, reason: holdsSubsections ? this.data.holdsSubsectionsReason : null };
		});
	}
	pickPlace(row: PlaceRow): void { if (!row.reason) this.place = row.id; }

	/** Rules in the copied part that show something outside it: they are dropped. */
	get droppedRules(): { from: string, to: string }[] {
		const value = (this.data.entry.form as UntypedFormGroup).getRawValue();
		const payload: TransferPayload = this.isSection ? { sourceLabel: '', sections: [value], questions: [] } : { sourceLabel: '', sections: [], questions: [value] };
		const byId = new Map<string, ToCEntry>();
		const visit = (list: ToCEntry[]) => list?.forEach(e => {
			byId.set(e.id, e);
			if (e.type === ToCEntryType.FieldSet) ((e.form.get('fields') as any)?.controls ?? []).forEach(f => byId.set(f.get('id').value, e));
			visit(e.subEntries);
		});
		visit(this.data.entries);
		return StructureTransfer.rulesLeaving(payload, id => { const e = byId.get(id); return e ? `${e.numbering} ${e.label}` : '?'; });
	}

	copyThere(): void { if (this.destination && this.place) this.dialogRef.close({ targetId: this.place, copy: true, external: this.destination }); }
}
