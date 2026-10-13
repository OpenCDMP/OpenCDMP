import { Directive, ElementRef, Input, NgZone, OnChanges, OnDestroy, OnInit } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { objectsOf, optionSemanticsOf } from '../semantic-objects';
import { StructureEditorBase } from '../structure-editor-base';

const MAP_CLASS = 'se2-map';
const FOCUS_CLASS = 'se2-preview-focus';
const FOCUS_MS = 3000;

/**
 * On the Review tab's author preview: with `appSe2PreviewMappings` on, each input carrying keys shows them above it
 * (and the question's objects), so the mapping can be read in place. It also scrolls to and highlights the question a
 * review item points at. The preview is the real author form, which knows nothing of mappings, so the chips are added
 * next to its inputs (found by their field id) and kept in place while the form re-renders.
 */
@Directive({ selector: '[appSe2PreviewMappings]' })
export class PreviewMappingsDirective implements OnInit, OnChanges, OnDestroy {
	@Input('appSe2PreviewMappings') show = false;

	private observer: MutationObserver;
	private pending: ReturnType<typeof setTimeout>;
	private focusTimer: ReturnType<typeof setTimeout>;
	private focus$: Subscription;

	constructor(private host: ElementRef<HTMLElement>, private editor: StructureEditorBase<any, any>, private zone: NgZone) { }

	ngOnInit(): void {
		this.focus$ = this.editor.previewFocus$.subscribe(id => this.focus(id));
	}

	ngOnChanges(): void {
		if (this.show) this.start(); else this.stop();
	}

	ngOnDestroy(): void {
		this.stop();
		this.focus$?.unsubscribe();
		clearTimeout(this.focusTimer);
	}

	private start(): void {
		this.apply();
		if (this.observer) return;
		this.zone.runOutsideAngular(() => {
			this.observer = new MutationObserver(records => {
				// our own chips are mutations too; only re-apply when the form itself changed
				const external = records.some(r => [...Array.from(r.addedNodes), ...Array.from(r.removedNodes)].some(n => !(n instanceof HTMLElement && n.classList.contains(MAP_CLASS))));
				if (!external) return;
				clearTimeout(this.pending);
				this.pending = setTimeout(() => this.apply(), 150);
			});
			this.observer.observe(this.host.nativeElement, { childList: true, subtree: true });
		});
	}

	private stop(): void {
		this.observer?.disconnect();
		this.observer = null;
		clearTimeout(this.pending);
		this.host.nativeElement.querySelectorAll(`.${MAP_CLASS}`).forEach(n => n.remove());
	}

	/** Adds the chips an input is missing; inputs the preview does not show (conditional ones) are skipped. */
	private apply(): void {
		if (!this.show) return;
		const root = this.host.nativeElement;
		this.editor.allFieldSets().forEach(question => {
			const objects = objectsOf(question);
			const fields = ((question.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
			fields.forEach((field, i) => {
				const id = field.get('id')?.value as string;
				const keys = [...new Set([...((field.get('semantics')?.value ?? []) as string[]), ...optionSemanticsOf(field).map(x => x.key)].filter(Boolean))];
				const shownObjects = i === 0 ? objects : [];
				if (!id || (!keys.length && !shownObjects.length)) return;
				const element = root.querySelector(`[id="${CSS.escape(id)}"]`);
				if (!element?.parentNode) return;
				const previous = element.previousElementSibling;
				if (previous?.classList.contains(MAP_CLASS) && previous.getAttribute('data-for') === id) return;
				const chips = document.createElement('div');
				chips.className = MAP_CLASS;
				chips.setAttribute('data-for', id);
				chips.setAttribute('aria-hidden', 'true');
				shownObjects.forEach(o => chips.appendChild(this.chip(o, true)));
				keys.forEach(k => chips.appendChild(this.chip(k, false)));
				element.parentNode.insertBefore(chips, element);
			});
		});
	}

	private chip(key: string, object: boolean): HTMLElement {
		const chip = document.createElement('span');
		chip.className = object ? `${MAP_CLASS}__chip ${MAP_CLASS}__chip--object` : `${MAP_CLASS}__chip`;
		chip.title = key;
		// the last two names say enough in place, e.g. …distribution.data_access
		const parts = key.split('.');
		chip.textContent = parts.length > 3 ? '…' + parts.slice(-2).join('.') : key;
		return chip;
	}

	private focus(questionId: string): void {
		const question = this.editor.allFieldSets().find(f => f.get('id')?.value === questionId);
		const ids = (((question?.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[]).map(f => f.get('id')?.value as string).filter(Boolean);
		const root = this.host.nativeElement;
		const element = ids.map(id => root.querySelector(`[id="${CSS.escape(id)}"]`)).find(Boolean) as HTMLElement;
		if (!element) return;
		const target = (element.closest('app-form-field-set') as HTMLElement) ?? element;
		root.querySelectorAll(`.${FOCUS_CLASS}`).forEach(n => n.classList.remove(FOCUS_CLASS));
		target.classList.add(FOCUS_CLASS);
		target.scrollIntoView({ behavior: 'smooth', block: 'center' });
		clearTimeout(this.focusTimer);
		this.focusTimer = setTimeout(() => target.classList.remove(FOCUS_CLASS), FOCUS_MS);
	}
}
