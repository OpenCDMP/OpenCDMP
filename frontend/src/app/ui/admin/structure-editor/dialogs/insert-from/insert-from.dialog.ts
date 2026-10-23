import { Component, Inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent } from '@angular/material/dialog';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { DescriptionTemplateEditorResolver } from '@app/ui/admin/description-template/editor/description-template-editor.resolver';
import { PlanBlueprintEditorResolver } from '@app/ui/admin/plan-blueprint/editor/plan-blueprint-editor.resolver';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ListingScope } from '@common/modules/admin-listing/admin-listing.component';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { AuthService } from '@app/core/services/auth/auth.service';
import { Guid } from '@common/types/guid';
import { forkJoin, merge, Observable, of, Subject } from 'rxjs';
import { catchError, debounceTime, map, startWith, switchMap, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { SourceNode, StructureTransfer, TransferItem, TransferPayload } from '../../structure-transfer';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatFormField, MatLabel, MatPrefix } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { MatCheckbox } from '@angular/material/checkbox';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

export interface InsertFromData {
	/** The item being edited, left out of the sources. */
	currentId: Guid;
	/** Where questions go: the section open in the pane, or null. */
	sectionLabel: string;
	/** The top-level section copied sections are inserted after, or null for the end. */
	rootLabel: string;
	sectionTakesQuestions: boolean;
	term: (key: string) => string;
}

/** Items of either kind that can be listed as sources, by name. */
export function findStructures(templates: DescriptionTemplateService, blueprints: PlanBlueprintService, like: string, onlyDraftsOfTenant: boolean, excludeId: Guid, sources?: { scope: ListingScope, tenantSelected: boolean, withBlueprints: boolean }): Observable<TransferItem[]> {
	const templateLookup = new DescriptionTemplateLookup();
	templateLookup.page = { offset: 0, size: 20 };
	templateLookup.like = like || null;
	templateLookup.isActive = [IsActive.Active];
	// own rows first (a null tenant sorts last), so the first page of "All" isn't only shared ones
	templateLookup.order = { items: [nameof<DescriptionTemplate>(x => x.belongsToCurrentTenant), nameof<DescriptionTemplate>(x => x.label)] };
	templateLookup.project = { fields: [nameof<DescriptionTemplate>(x => x.id), nameof<DescriptionTemplate>(x => x.label), nameof<DescriptionTemplate>(x => x.version), nameof<DescriptionTemplate>(x => x.status), nameof<DescriptionTemplate>(x => x.belongsToCurrentTenant)] };
	const blueprintLookup = new PlanBlueprintLookup();
	blueprintLookup.page = { offset: 0, size: 20 };
	blueprintLookup.like = like || null;
	blueprintLookup.isActive = [IsActive.Active];
	blueprintLookup.order = { items: [nameof<PlanBlueprint>(x => x.belongsToCurrentTenant), nameof<PlanBlueprint>(x => x.label)] };
	blueprintLookup.project = { fields: [nameof<PlanBlueprint>(x => x.id), nameof<PlanBlueprint>(x => x.label), nameof<PlanBlueprint>(x => x.version), nameof<PlanBlueprint>(x => x.status), nameof<PlanBlueprint>(x => x.belongsToCurrentTenant)] };
	if (onlyDraftsOfTenant) {
		templateLookup.statuses = [0];
		blueprintLookup.statuses = [0];
	}
	if (sources) {
		// the same rules as the admin listings: templates and blueprints you can edit, and the tenant scope tabs
		templateLookup.onlyCanEdit = true;
		blueprintLookup.onlyCanEdit = true;
		const tenantIsSet = sources.scope === 'own' && sources.tenantSelected ? true : sources.scope === 'shared' ? false : undefined;
		templateLookup.tenantIsSet = tenantIsSet;
		blueprintLookup.tenantIsSet = tenantIsSet;
	}
	return forkJoin([
		templates.query(templateLookup).pipe(map(r => (r.items ?? []).map(x => ({ kind: 'template' as const, id: x.id, label: x.label, version: x.version, status: x.status, belongsToCurrentTenant: x.belongsToCurrentTenant }))), catchError(() => of([]))),
		sources && !sources.withBlueprints ? of([] as TransferItem[]) : blueprints.query(blueprintLookup).pipe(map(r => (r.items ?? []).map(x => ({ kind: 'blueprint' as const, id: x.id, label: x.label, version: x.version, status: x.status, belongsToCurrentTenant: x.belongsToCurrentTenant }))), catchError(() => of([])))
	]).pipe(map(([a, b]) => [...a, ...b]
		.filter(x => x.id?.toString() !== excludeId?.toString())
		// only this tenant's own drafts can take a copy; shared ones are read-only here
		.filter(x => !onlyDraftsOfTenant || x.belongsToCurrentTenant !== false)
		.sort((a, b) => Number(a.belongsToCurrentTenant === false) - Number(b.belongsToCurrentTenant === false))));
}

/** The full definition of a source, as a numbered tree. */
export function loadStructure(templates: DescriptionTemplateService, blueprints: PlanBlueprintService, item: TransferItem): Observable<SourceNode[]> {
	const request: Observable<any> = item.kind === 'template'
		? templates.getSingle(item.id, DescriptionTemplateEditorResolver.lookupFields())
		: blueprints.getSingle(item.id, PlanBlueprintEditorResolver.lookupFields());
	return request.pipe(map(x => StructureTransfer.buildSourceTree(x?.definition?.sections ?? [])));
}

/**
 * Pull sections or questions from any template or blueprint you can see, shared ones included. They are copied here
 * with new ids; the source is untouched. The dialog says what the copy keeps and what it drops before anything happens.
 */
@Component({
    selector: 'app-bp2-insert-from-dialog',
    templateUrl: './insert-from.dialog.html',
    styleUrls: ['./insert-from.dialog.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatButtonToggleGroup, MatButtonToggle, MatFormField, MatLabel, MatIcon, MatPrefix, ReactiveAsteriskDirective, MatInput, FormsModule, ReactiveFormsModule, MatButton, MatCheckbox, TranslatePipe, Bp2CountPipe]
})
export class InsertFromDialogComponent implements OnInit {
	search = new FormControl('');
	/** Owned / shared / all, like the admin listings; only offered when a tenant other than the default is selected. */
	scope: ListingScope = 'all';
	readonly tenantSelected = !!this.authService.getSelectedTenantId();
	readonly tenantName = this.authService.getSelectedTenantName();
	private readonly scopeChanged = new Subject<void>();
	items: TransferItem[] = null;
	source: TransferItem = null;
	tree: SourceNode[] = null;
	loading = false;
	private selected = new Set<string>();

	constructor(
		public dialogRef: MatDialogRef<InsertFromDialogComponent, TransferPayload>,
		@Inject(MAT_DIALOG_DATA) public data: InsertFromData,
		private templates: DescriptionTemplateService,
		private blueprints: PlanBlueprintService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private authService: AuthService,
	) { }

	ngOnInit(): void {
		const sources = () => ({ scope: this.scope, tenantSelected: this.tenantSelected, withBlueprints: this.authService.hasPermission(AppPermission.ViewPlanBlueprintPage) });
		merge(this.search.valueChanges.pipe(startWith(''), debounceTime(250)), this.scopeChanged).pipe(
			switchMap(() => findStructures(this.templates, this.blueprints, this.search.value?.trim(), false, this.data.currentId, sources())),
			takeUntil(this.dialogRef.afterClosed())
		).subscribe(items => this.items = items);
	}

	t(key: string): string { return this.data.term(key); }

	setScope(scope: ListingScope): void { this.scope = scope; this.items = null; this.scopeChanged.next(); }

	pick(item: TransferItem): void {
		this.source = item;
		this.tree = null;
		this.selected.clear();
		this.loading = true;
		loadStructure(this.templates, this.blueprints, item).subscribe({
			next: tree => { this.tree = tree; this.loading = false; },
			error: error => { this.loading = false; this.source = null; this.httpErrorHandlingService.handleBackedRequestError(error); }
		});
	}
	back(): void { this.source = null; this.tree = null; this.selected.clear(); }

	get rows(): SourceNode[] { return StructureTransfer.flatten(this.tree ?? []); }
	depth(node: SourceNode): number { let d = 0; let p = node.parent; while (p) { d++; p = p.parent; } return d; }
	/** Included because a section above it is selected. */
	covered(node: SourceNode): boolean { let p = node.parent; while (p) { if (this.selected.has(p.id)) return true; p = p.parent; } return false; }
	isChecked(node: SourceNode): boolean { return this.selected.has(node.id) || this.covered(node); }
	toggle(node: SourceNode): void {
		if (this.covered(node)) return;
		if (this.selected.has(node.id)) { this.selected.delete(node.id); return; }
		this.selected.add(node.id);
		// a selected section carries everything in it
		StructureTransfer.flatten(node.children).forEach(c => this.selected.delete(c.id));
	}

	isPreset(node: SourceNode): boolean { return StructureTransfer.presetOf(node.value) != null; }

	get payload(): TransferPayload {
		const picked = this.rows.filter(n => this.selected.has(n.id) && !this.covered(n));
		return {
			sourceLabel: this.source?.label ?? '',
			sections: picked.filter(n => n.kind === 'section').map(n => n.value),
			questions: picked.filter(n => n.kind === 'question').map(n => n.value)
		};
	}
	get sectionCount(): number { return this.payload.sections.length; }
	get questionCount(): number { const p = this.payload; return p.questions.length + p.sections.reduce((n, s) => n + StructureTransfer.fieldSetsIn(s).length, 0); }

	/** Rules in the selection that point outside it, with the question they would have shown. */
	get droppedRules(): { from: string, to: string }[] {
		const byId = new Map<string, SourceNode>();
		this.rows.forEach(n => { byId.set(n.id, n); if (n.kind === 'question') (n.value?.fields ?? []).forEach((f: any) => f?.id && byId.set(f.id.toString(), n)); });
		return StructureTransfer.rulesLeaving(this.payload, id => { const n = byId.get(id); return n ? `${n.numbering} ${n.title}` : '?'; });
	}

	insert(): void { if (this.sectionCount || this.questionCount) this.dialogRef.close(this.payload); }
}
