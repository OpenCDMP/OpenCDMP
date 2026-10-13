import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogTitle, MatDialogContent } from '@angular/material/dialog';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';

export interface CompareVersionsData {
	/** The version being edited, as persist-shaped values. */
	current: any;
	/** The version to compare with, as returned by the API. */
	other: any;
	currentVersion: number;
	otherVersion: number;
}

type Change = { kind: 'added' | 'removed' | 'changed', where: string, what: string, detail?: string };

interface Item { id: string, path: string, title: string, section: boolean, signature: string }

/**
 * What differs from the version authors get today: sections and questions added, removed or changed, and the
 * details. Matching is by id, which a new version keeps from the one it was made from.
 */
@Component({
    selector: 'app-bp2-compare-versions-dialog',
    templateUrl: './compare-versions.dialog.html',
    styleUrls: ['./compare-versions.dialog.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, CdkScrollable, MatDialogContent, MatIcon, MatButton, TranslatePipe]
})
export class CompareVersionsDialogComponent {
	changes: Change[] = [];

	constructor(public dialogRef: MatDialogRef<CompareVersionsDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: CompareVersionsData, private language: TranslateService) {
		this.changes = this.diff();
	}

	get added(): Change[] { return this.changes.filter(c => c.kind === 'added'); }
	get removed(): Change[] { return this.changes.filter(c => c.kind === 'removed'); }
	get changed(): Change[] { return this.changes.filter(c => c.kind === 'changed'); }

	private flatten(definition: any): Item[] {
		const items: Item[] = [];
		const visit = (sections: any[], prefix: string) => (sections ?? []).forEach((s, i) => {
			const path = prefix ? `${prefix}.${i + 1}` : `${i + 1}`;
			items.push({ id: s.id?.toString(), path, title: s.title ?? '', section: true, signature: [s.title, this.plain(s.description), s.hasTemplates, (s.descriptionTemplates ?? []).length, this.objects(s)].join('|') });
			(s.fieldSets ?? []).forEach((q, j) => items.push({ id: q.id?.toString(), path: `${path}.${j + 1}`, title: q.title ?? '', section: false, signature: this.questionSignature(q) }));
			visit(s.sections, path);
		});
		visit(definition?.sections, '');
		return items;
	}

	private questionSignature(q: any): string {
		const fields = (q.fields ?? []).map(f => [f.data?.fieldType, f.data?.systemFieldType, (f.validations ?? []).join(','), (f.semantics ?? []).join(','), f.includeInExport, (f.visibilityRules ?? []).length, this.optionsSignature(f.data?.options)].join('/'));
		return [q.title, this.plain(q.description), this.plain(q.extendedDescription), q.additionalInformation, q.hasMultiplicity, q.hasCommentField, this.objects(q), fields.join(';')].join('|');
	}

	private objects(form: any): string { return (form?.semantics ?? []).filter(k => !!k?.trim()).join(','); }

	/** Field by field, so the key order and the empty keys a form adds (`semantics: []`) do not read as a change. */
	private optionsSignature(options: any[]): string {
		return (options ?? []).map(o => [o.label ?? '', o.value ?? '', !!o.exportsNothing, (o.semantics ?? []).map(x => `${x?.key ?? ''}:${x?.value ?? ''}`).join(',')].join('=')).join('~');
	}

	private plain(html: string): string { return (html ?? '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim(); }

	private diff(): Change[] {
		const t = (k: string, p?: object) => this.language.instant(k, p);
		const changes: Change[] = [];
		const a = this.data.current, b = this.data.other;
		const detail = (key: string, x: any, y: any) => { if ((x ?? '') !== (y ?? '')) changes.push({ kind: 'changed', where: t('PLAN-BLUEPRINT-EDITOR.TABS.DETAILS'), what: t(key) }); };
		detail('PLAN-BLUEPRINT-EDITOR.DETAILS.NAME', a?.label, b?.label);
		detail('PLAN-BLUEPRINT-EDITOR.DETAILS.DESCRIPTION', this.plain(a?.description), this.plain(b?.description));
		const ca = a?.definition?.planCreationConsent ?? {}, cb = b?.definition?.planCreationConsent ?? {};
		if (!!ca.enabled !== !!cb.enabled || (ca.message ?? '') !== (cb.message ?? '') || this.plain(ca.details) !== this.plain(cb.details)) changes.push({ kind: 'changed', where: t('PLAN-BLUEPRINT-EDITOR.TABS.DETAILS'), what: t('PLAN-BLUEPRINT-EDITOR.CONSENT.TITLE') });

		const now = this.flatten(a?.definition), before = this.flatten(b?.definition);
		const beforeById = new Map(before.map(i => [i.id, i]));
		const nowIds = new Set(now.map(i => i.id));
		const noun = (i: Item) => t(i.section ? 'PLAN-BLUEPRINT-EDITOR.SECTION.SECTION' : 'PLAN-BLUEPRINT-EDITOR.RULES.QUESTION');
		now.forEach(i => {
			const old = beforeById.get(i.id);
			if (!old) { changes.push({ kind: 'added', where: `${i.path} ${i.title || ''}`.trim(), what: noun(i) }); return; }
			if (old.signature !== i.signature) changes.push({ kind: 'changed', where: `${i.path} ${i.title || ''}`.trim(), what: noun(i), detail: old.title !== i.title ? t('PLAN-BLUEPRINT-EDITOR.COMPARE.WAS', { title: old.title }) : null });
			else if (old.path !== i.path) changes.push({ kind: 'changed', where: `${i.path} ${i.title || ''}`.trim(), what: noun(i), detail: t('PLAN-BLUEPRINT-EDITOR.COMPARE.MOVED', { from: old.path }) });
		});
		before.filter(i => !nowIds.has(i.id)).forEach(i => changes.push({ kind: 'removed', where: `${i.path} ${i.title || ''}`.trim(), what: noun(i) }));
		return changes;
	}
}
