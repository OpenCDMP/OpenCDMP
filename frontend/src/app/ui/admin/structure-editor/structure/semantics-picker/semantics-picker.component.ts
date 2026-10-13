import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { AbstractControl, UntypedFormArray } from '@angular/forms';
import { SemanticsLookup } from '@app/core/query/semantic.lookup';
import { SemanticsService } from '@app/core/services/semantic/semantics.service';
import { Observable, of, Subject } from 'rxjs';
import { catchError, shareReplay, takeUntil } from 'rxjs/operators';
import { questionIndex } from '../../input-kinds';
import { objectOfKey, shortKey } from '../../semantic-objects';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

interface Choice { key: string, kind: 'typed' | 'used' | 'tenant' | 'catalogue' | 'plugin' | 'object', group?: string, count?: number, label?: string, sub?: string }

/** Loaded once per page: the catalogue is a fixed list of suggestions, not something that changes while editing. */
let catalogue$: Observable<string[]> = null;
let nextId = 0;

/**
 * Semantics are free text: any key is valid and travels with the answer for plugins and external systems to
 * read. The catalogue only suggests. Typing and pressing Enter adds exactly what was typed.
 *
 * In `objects` mode it picks what a section or question describes instead, suggesting the objects the plugins
 * declare. With a `scope` (the objects around an input), keys under them are suggested first and shown shortened
 * (`…host.url`); the full key is always what is stored. `single` holds one key in a string control.
 */
@Component({
    selector: 'app-bp2-semantics-picker',
    templateUrl: './semantics-picker.component.html',
    styleUrls: ['./semantics-picker.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, ReactiveAsteriskDirective, TranslatePipe, Bp2CountPipe]
})
export class Bp2SemanticsPickerComponent implements OnInit, OnDestroy {
	@Input() control: AbstractControl;
	@Input() inputLabel: string;
	@Input() mode: 'keys' | 'objects' = 'keys';
	/** The objects around the input, innermost first: keys under them are suggested first and shown shortened. */
	@Input() scope: string[] = [];
	/** One key in a string control, e.g. an answer's key row. */
	@Input() single = false;
	@Input() ariaLabel: string;
	@ViewChild('search') search: ElementRef<HTMLInputElement>;

	readonly listId = `bp2-sem-list-${nextId++}`;
	query = '';
	open = false;
	active = 0;
	catalogue: string[] = [];
	private destroyed = new Subject<void>();

	constructor(public editor: StructureEditorBase<any, any>, private semanticsService: SemanticsService) { }

	ngOnInit(): void {
		if (this.mode === 'objects') return;
		if (!catalogue$) catalogue$ = this.semanticsService.searchSemantics(new SemanticsLookup()).pipe(catchError(() => of([] as string[])), shareReplay(1));
		catalogue$.pipe(takeUntil(this.destroyed)).subscribe(keys => this.catalogue = keys ?? []);
	}
	ngOnDestroy(): void { this.destroyed.next(); this.destroyed.complete(); }

	get readOnly(): boolean { return this.editor.readOnly; }
	get keys(): string[] {
		const value = this.control?.value;
		if (this.single) return value ? [value as string] : [];
		return value ?? [];
	}
	get groupCount(): number { return new Set(this.catalogue.map(k => this.prefix(k))).size; }

	prefix(key: string): string { return key.includes('.') ? key.split('.')[0] : key; }
	rest(key: string): string { return key.includes('.') ? key.slice(key.indexOf('.') + 1) : ''; }
	/** The key as shown: shortened under an object around the input. */
	shown(key: string): string { return this.mode === 'keys' ? shortKey(key, this.scope) : key; }
	isShort(key: string): boolean { return this.shown(key) !== key; }

	/** Keys other inputs of this blueprint already carry, most used first. */
	private usedElsewhere(): { key: string, count: number }[] {
		const counts = new Map<string, number>();
		questionIndex(this.editor.toCEntries).forEach(q => ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []).forEach(f => {
			if (f.get('semantics') === this.control) return;
			((f.get('semantics')?.value ?? []) as string[]).forEach(k => counts.set(k, (counts.get(k) ?? 0) + 1));
		}));
		return [...counts.entries()].map(([key, count]) => ({ key, count })).sort((a, b) => b.count - a.count);
	}

	/** Grouped for display; the flat list drives keyboard navigation. */
	private memoKey: string = null;
	private memo: { title: string, sub?: string, items: Choice[] }[] = [];

	get sections(): { title: string, sub?: string, items: Choice[] }[] {
		const key = `${this.query}|${this.keys.join(',')}|${this.catalogue.length}|${this.open}|${this.editor.pluginSemantics.length}|${this.scope?.join(',')}`;
		if (key === this.memoKey) return this.memo;
		this.memoKey = key;
		this.memo = this.mode === 'objects' ? this.buildObjectSections() : this.buildSections();
		return this.memo;
	}

	private buildObjectSections(): { title: string, sub?: string, items: Choice[] }[] {
		const q = this.query.trim().toLowerCase();
		const taken = new Set(this.keys);
		const result: { title: string, sub?: string, items: Choice[] }[] = [];
		if (q && !taken.has(this.query.trim())) result.push({ title: null, items: [{ key: this.query.trim(), kind: 'typed' }] });
		const objects = this.editor.knownObjects();
		const labelOf = (key: string) => objects.find(o => o.key === key)?.label ?? key;
		const matches = objects.filter(o => !taken.has(o.key) && (!q || o.key.toLowerCase().includes(q) || (o.label ?? '').toLowerCase().includes(q)));
		if (matches.length) result.push({
			title: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.OBJECTS.SUGGESTED'),
			items: matches.slice(0, q ? 12 : 8).map(o => ({
				key: o.key, kind: 'object', label: o.label,
				sub: [
					this.editor.language.instant(o.multiple ? 'PLAN-BLUEPRINT-EDITOR.OBJECTS.LIST' : 'PLAN-BLUEPRINT-EDITOR.OBJECTS.SINGLE'),
					o.parent ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.OBJECTS.INSIDE', { parent: labelOf(o.parent) }) : null,
					o.readers.join(', ')
				].filter(Boolean).join(' · ')
			}))
		});
		return result;
	}

	private buildSections(): { title: string, sub?: string, items: Choice[] }[] {
		const q = this.query.trim().toLowerCase();
		const taken = new Set(this.keys);
		const result: { title: string, sub?: string, items: Choice[] }[] = [];
		if (q && !taken.has(this.query.trim())) result.push({ title: null, items: [{ key: this.query.trim(), kind: 'typed' }] });
		const matchesQuery = (k: string) => !q || k.toLowerCase().includes(q) || this.shown(k).toLowerCase().includes(q);
		// keys the plugins read come first; under an object, the object's own keys lead
		const shown = new Set<string>();
		const usages = this.editor.knownUsages();
		const pluginKeys: Choice[] = [];
		usages.forEach(({ usage }) => (usage.keys ?? []).forEach(k => {
			if (taken.has(k) || shown.has(k) || !matchesQuery(k)) return;
			shown.add(k);
			pluginKeys.push({ key: k, kind: 'plugin', label: usage.label });
		}));
		if (this.scope?.length) {
			const inside = pluginKeys.filter(c => objectOfKey(c.key, this.scope));
			if (inside.length) result.push({ title: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.SEMANTICS.UNDER-OBJECT', { object: this.scope[0] }), items: inside.slice(0, q ? 12 : 8) });
			inside.forEach(c => shown.add(c.key));
		}
		const outside = pluginKeys.filter(c => !this.scope?.length || !objectOfKey(c.key, this.scope));
		if (outside.length && (q || !this.scope?.length)) result.push({ title: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.SEMANTICS.READ-BY-PLUGINS'), items: outside.slice(0, q ? 8 : 4) });
		outside.slice(0, q ? 8 : 4).forEach(c => shown.add(c.key));

		const used = this.usedElsewhere().filter(u => !taken.has(u.key) && !shown.has(u.key) && matchesQuery(u.key)).slice(0, 5);
		if (used.length) result.push({ title: this.editor.language.instant(this.editor.term('PLAN-BLUEPRINT-EDITOR.SEMANTICS.USED-HERE')), items: used.map(u => ({ key: u.key, kind: 'used' })) });
		// keys the tenant's other templates or blueprints carry come next, so custom vocabularies stay consistent
		used.forEach(u => shown.add(u.key));
		const tenant = this.editor.tenantSemantics.filter(u => !taken.has(u.key) && !shown.has(u.key) && matchesQuery(u.key)).slice(0, 5);
		if (tenant.length) result.push({ title: this.editor.language.instant(this.editor.term('PLAN-BLUEPRINT-EDITOR.SEMANTICS.USED-TENANT')), items: tenant.map(u => ({ key: u.key, kind: 'tenant', count: u.count })) });
		tenant.forEach(u => shown.add(u.key));
		const matches = this.catalogue.filter(k => !taken.has(k) && !shown.has(k) && matchesQuery(k));
		const byGroup = new Map<string, string[]>();
		matches.forEach(k => { const g = this.prefix(k); if (!byGroup.has(g)) byGroup.set(g, []); byGroup.get(g).push(k); });
		[...byGroup.entries()].slice(0, q ? 6 : 3).forEach(([group, keys]) => result.push({
			title: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.SEMANTICS.SUGGESTIONS', { group }),
			items: keys.slice(0, q ? 6 : 3).map(key => ({ key, kind: 'catalogue', group }))
		}));
		return result;
	}
	get flat(): Choice[] { return this.sections.flatMap(s => s.items); }

	onInput(value: string): void { this.query = value; this.open = true; this.active = 0; }
	onFocus(): void { this.open = true; }
	onBlur(): void { setTimeout(() => this.open = false, 150); }

	onKeydown(event: KeyboardEvent): void {
		const items = this.flat;
		if (event.key === 'ArrowDown') { this.active = Math.min(items.length - 1, this.active + 1); this.open = true; event.preventDefault(); }
		else if (event.key === 'ArrowUp') { this.active = Math.max(0, this.active - 1); event.preventDefault(); }
		else if (event.key === 'Enter') {
			event.preventDefault();
			const pick = items[this.active] ?? (this.query.trim() ? { key: this.query.trim(), kind: 'typed' } as Choice : null);
			if (pick) this.add(pick.key);
		} else if (event.key === 'Escape') { this.open = false; }
		else if (event.key === 'Backspace' && !this.query && this.keys.length) { this.remove(this.keys.length - 1); }
	}

	add(key: string): void {
		const k = (key ?? '').trim();
		if (!k || this.readOnly || this.keys.includes(k)) return;
		this.control.setValue(this.single ? k : [...this.keys, k]);
		this.control.markAsDirty();
		this.query = ''; this.active = 0;
		if (this.search) this.search.nativeElement.value = '';
		// one key per row, and usually one object per section or question: close the list after a pick
		if (this.single || this.mode === 'objects') { this.open = false; this.search?.nativeElement.blur(); return; }
		this.search?.nativeElement.focus();
	}

	remove(index: number): void {
		if (this.readOnly) return;
		if (this.single) { this.control.setValue(null); this.control.markAsDirty(); return; }
		const next = [...this.keys]; next.splice(index, 1);
		this.control.setValue(next);
		this.control.markAsDirty();
	}

	indexOf(choice: Choice): number { return this.flat.findIndex(c => c.key === choice.key && c.kind === choice.kind); }
}
