import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { PluginSemantics } from '@app/core/model/semantic/plugin-semantics';
import { questionIndex, QuestionRef } from '../../input-kinds';
import { needsTone, NeedsTone, PluginCoverage, pluginCoverage, pluginKindKey, ValueStatus } from '../../plugin-coverage';
import { SemanticCheck } from '../../semantic-checks';
import { objectsOf, optionSemanticsOf } from '../../semantic-objects';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';
import { Bp2KeyPartsPipe } from '../../key-parts.pipe';

/** A question or section a problem is about, numbered as the outline numbers it. */
export interface ReviewTarget { id: string; numbering: string; title: string; sectionId: string; isSection: boolean; }

/** Something to fix: a needed value nothing fills, or a mapping that won't come out right. */
interface FixItem { key: string; text: string; targets: ReviewTarget[]; }

/** One plugin, with its problems ordered by what to do about them. */
interface PluginReview {
	id: string;
	name: string;
	coverage: PluginCoverage;
	tone: NeedsTone;
	fix: FixItem[];
	fallback: ValueStatus[];
	notices: FixItem[];
	couldUse: ValueStatus[];
	/** Values the attached templates fill, and the templates that fill them. */
	fromTemplates: ValueStatus[];
	templates: string[];
}

/**
 * What each installed plugin will do with the structure, in one block per plugin: what to fix first (needed values
 * nothing fills, and mappings that won't come out right), what the plugin fills with a fallback, notices, and a count
 * of the values it could also use. Replaces the separate standard mappings, coverage and checks cards.
 */
@Component({
    selector: 'app-se2-plugin-review',
    templateUrl: './plugin-review.component.html',
    styleUrls: ['./plugin-review.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, TranslatePipe, Bp2CountPipe, Bp2KeyPartsPipe]
})
export class PluginReviewComponent implements OnInit {
	/** Plugins whose "could also use" list is open, by id. */
	expanded = new Set<string>();

	constructor(public editor: StructureEditorBase<any, any>) { }

	ngOnInit(): void { this.editor.refreshAttachedUses(); }

	get reviews(): PluginReview[] {
		const use = this.editor.structureUse();
		const checks = this.editor.semanticChecks();
		const questions = questionIndex(this.editor.toCEntries);
		return pluginCoverage(this.editor.pluginSemantics, this.editor.pluginEntityType, use, this.editor.attachedUses).map(coverage => {
			const plugin = this.editor.pluginSemantics.find(p => p.pluginType === coverage.pluginType && p.pluginId === coverage.pluginId);
			const mine = checks.filter(c => this.reads(plugin, c.key));
			const gaps = coverage.needs.filter(x => !x.covered);
			const fromTemplates = [...coverage.needs, ...coverage.canUse].filter(x => x.viaTemplates?.length);
			const fix: FixItem[] = [
				...mine.filter(c => !c.notice).map(c => this.checkItem(c)),
				...gaps.filter(x => !x.usage.fallback).map(x => ({ key: x.usage.keys?.[0] ?? '', text: [x.usage.label, x.usage.purpose].filter(Boolean).join(' · '), targets: this.questionsUsing(questions, x.usage.object) })),
			];
			return {
				id: `${coverage.pluginType}:${coverage.pluginId}`,
				name: this.editor.language.instant(pluginKindKey(coverage.pluginType), { name: coverage.name }),
				coverage,
				tone: fix.length ? 'gap' : needsTone(coverage.needs),
				fix,
				fallback: gaps.filter(x => !!x.usage.fallback),
				notices: mine.filter(c => c.notice).map(c => this.checkItem(c)),
				couldUse: coverage.canUse.filter(x => !x.covered),
				fromTemplates,
				templates: [...new Set(fromTemplates.flatMap(x => x.viaTemplates))],
			};
		});
	}

	fallbackCount(r: PluginReview): number { return r.fallback.length; }
	percent(done: number, total: number): number { return total ? Math.round(done / total * 100) : 0; }
	keyOf(x: ValueStatus): string { return x.usage.keys?.[0] ?? ''; }
	titleOf(x: ValueStatus): string { return [x.usage.label, x.usage.purpose].filter(Boolean).join(' · '); }
	toggle(r: PluginReview): void { if (this.expanded.has(r.id)) this.expanded.delete(r.id); else this.expanded.add(r.id); }

	/**
	 * Shows a question in the author preview when it is there (scrolled to and highlighted), otherwise opens it in the
	 * Structure tab: conditional questions and sections are not in the preview until their condition holds.
	 */
	show(t: ReviewTarget): void {
		if (!t.isSection && this.focusInPreview(t.id)) return;
		this.open(t);
	}

	open(t: ReviewTarget): void {
		if (t.isSection) { this.editor.selectById(t.id); this.editor.setTab('structure'); return; }
		this.editor.openQuestion(t.id, t.sectionId);
	}

	private focusInPreview(questionId: string): boolean {
		const question = this.editor.allFieldSets().find(f => f.get('id')?.value === questionId);
		const fieldIds = (((question?.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[]).map(f => f.get('id')?.value as string).filter(Boolean);
		const preview = document.querySelector('.rv-preview');
		const element = fieldIds.map(id => preview?.querySelector(`[id="${CSS.escape(id)}"]`)).find(Boolean);
		if (!element) return false;
		this.editor.previewFocus$.next(questionId);
		return true;
	}

	private checkItem(c: SemanticCheck): FixItem {
		return { key: c.key, text: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.CHECKS.' + c.message, c.params), targets: c.targets };
	}

	/** Whether the plugin reads the key: one of its values, a value under it (an object), or one of its objects. */
	private reads(plugin: PluginSemantics, key: string): boolean {
		if (!plugin || !key) return false;
		const under = (k: string) => k === key || k.startsWith(key + '.') || key.startsWith(k + '.');
		return (plugin.semantics?.keys ?? []).some(u => (u.keys ?? []).some(under) || (u.object && under(u.object)))
			|| (plugin.semantics?.objects ?? []).some(o => under(o.key));
	}

	/** The questions that carry keys under an object, or describe it: where a value missing in it would go. */
	private questionsUsing(questions: QuestionRef[], object: string): ReviewTarget[] {
		if (!object) return [];
		const under = (k: string) => k === object || k?.startsWith(object + '.');
		return questions.filter(q => {
			const fields = ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
			return objectsOf(q.fieldSetForm).some(under)
				|| fields.some(f => ((f.get('semantics')?.value ?? []) as string[]).some(under) || optionSemanticsOf(f).some(x => under(x.key)));
		}).map(q => ({ id: q.id, numbering: q.numbering, title: q.title, sectionId: q.sectionIds[q.sectionIds.length - 1] ?? null, isSection: false }));
	}
}
