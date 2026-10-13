import { DestroyRef, Injectable, Injector } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, RouterStateSnapshot, TitleStrategy } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BreadCrumbRouteData, BreadcrumbService } from '../../../ui/misc/breadcrumb/breadcrumb.service';

interface TitleKey {
	key: string;
	params?: Record<string, string>;
	/** When true `key` is already a human-readable value (resolved entity name), not a language key. */
	literal?: boolean;
}

/**
 * Drives the browser-tab title from the per-route breadcrumb taxonomy.
 *
 * Replaces the old manual `NavigationEnd` title block in `AppComponent`, which only
 * read the *deepest* activated child's `data.title` — a value that, for non-empty-path
 * leaf routes, never inherits the parent's title and so fell back to a stale "Home".
 *
 * Instead this walks the whole route chain (root -> leaf), collects every declared
 * breadcrumb title, and composes `"OpenCDMP - <Section> - <Page>"` from the deepest
 * meaningful segments. Static keys, dynamic `titleFactory` keys and resolved entity
 * names (registered via {@link BreadcrumbService.addIdResolvedValue}) are all supported.
 * Entity editors therefore need no per-component title handling: their `:id` route resolves
 * the registered label via the path param, and create routes carry a static `title` key.
 *
 * DI note: `TranslateService` and `BreadcrumbService` are resolved lazily via `Injector`
 * (not constructor-injected). The Router pulls a `TitleStrategy` in eagerly at bootstrap;
 * `TranslateService` -> `TranslateServerLoader` -> `AuthService` -> `Router` would otherwise
 * form a construction-time circular dependency (NG0200). Resolving them on first
 * `updateTitle()` (during navigation, after the injector graph is built) avoids it.
 */
@Injectable({ providedIn: 'root' })
export class AppTitleStrategy extends TitleStrategy {
	/** Compiled to "OpenCDMP - " by the custom translation compiler ({{ APP_NAME_CAPS }}). */
	private static readonly PREFIX_KEY = 'GENERAL.TITLES.PREFIX';
	/** Shown when no route in the chain declares a title (mirrors the old fallback). */
	private static readonly FALLBACK_KEY = 'GENERAL.TITLES.GENERAL';
	private static readonly SEPARATOR = ' - ';
	private static readonly PART_MAX_LENGTH = 60;
	private static readonly MAX_CONTEXT_PARTS = 2;

	#lastSnapshot: RouterStateSnapshot | null = null;
	#resolvedValues: Record<string, string> = {};
	#translate: TranslateService | null = null;
	#breadcrumb: BreadcrumbService | null = null;
	#initialized = false;

	constructor(
		private readonly title: Title,
		private readonly injector: Injector,
		private readonly destroyRef: DestroyRef,
	) {
		super();
	}

	private get translate(): TranslateService {
		return (this.#translate ??= this.injector.get(TranslateService));
	}

	private get breadcrumb(): BreadcrumbService {
		return (this.#breadcrumb ??= this.injector.get(BreadcrumbService));
	}

	override updateTitle(snapshot: RouterStateSnapshot): void {
		this.#lastSnapshot = snapshot;
		this.#init();

		const leafData = this.#leaf(snapshot).data;

		const lang = this.translate.currentLang;
		if (!lang || !this.translate.langs?.includes(lang)) {
			// Translations for the active language are not loaded yet; keep the
			// index.html bootstrap <title> until a language is ready.
			return;
		}

		const parts = this.#collectTitleKeys(snapshot.root)
			.map(tk => this.#clamp(this.#translateKey(tk)))
			.filter(Boolean);

		const context = parts.slice(-AppTitleStrategy.MAX_CONTEXT_PARTS);

		if (!context.length) {
			this.title.setTitle(this.translate.instant(AppTitleStrategy.FALLBACK_KEY));
			return;
		}

		const usePrefix = leafData?.['usePrefix'] ?? true;
		const prefix = usePrefix ? this.translate.instant(AppTitleStrategy.PREFIX_KEY) : '';
		this.title.setTitle(prefix + context.join(AppTitleStrategy.SEPARATOR));
	}

	/**
	 * Wire up the language-change and resolved-value re-renders on first use. Deferred out of
	 * the constructor so `TranslateService`/`BreadcrumbService` aren't resolved at bootstrap.
	 */
	#init(): void {
		if (this.#initialized) {
			return;
		}
		this.#initialized = true;

		// Re-localise the tab title when the active language changes.
		this.translate.onLangChange
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.#reRender());

		// Refresh when async entity names resolve (e.g. a plan name registered after
		// navigation). The first emission is the current state and is skipped.
		let first = true;
		this.breadcrumb.resolvedValues()
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(values => {
				this.#resolvedValues = values ?? {};
				if (first) {
					first = false;
					return;
				}
				this.#reRender();
			});
	}

	/** Re-run composition for the last navigation (after a language or resolved-value change). */
	#reRender(): void {
		if (this.#lastSnapshot) {
			this.updateTitle(this.#lastSnapshot);
		}
	}

	#leaf(snapshot: RouterStateSnapshot): ActivatedRouteSnapshot {
		let node = snapshot.root;
		while (node.firstChild) {
			node = node.firstChild;
		}
		return node;
	}

	#collectTitleKeys(root: ActivatedRouteSnapshot): TitleKey[] {
		const chain: TitleKey[] = [];
		for (let node: ActivatedRouteSnapshot | null = root; node; node = node.firstChild) {
			const tk = this.#extractTitleKey(node);
			if (tk?.key) {
				chain.push(tk);
			}
		}

		// Drop consecutive duplicates (a route often declares the same key on both
		// `data.breadcrumb.title` and `data.title`, and parents can repeat a child key).
		const deduped: TitleKey[] = [];
		for (const tk of chain) {
			if (deduped[deduped.length - 1]?.key !== tk.key) {
				deduped.push(tk);
			}
		}
		return deduped;
	}

	#extractTitleKey(node: ActivatedRouteSnapshot): TitleKey | null {
		// Use the route's OWN config data (not the inherited snapshot data) so each
		// segment contributes its own title exactly once.
		const data = node.routeConfig?.data;
		if (!data) {
			return null;
		}

		const raw = data[BreadcrumbService.ROUTE_DATA_KEY];
		const breadcrumbData: BreadCrumbRouteData | undefined =
			raw && typeof raw === 'object' ? raw : undefined;

		if (breadcrumbData?.title) {
			return { key: breadcrumbData.title };
		}

		if (breadcrumbData?.titleFactory) {
			const { languageKey, translateParams } = breadcrumbData.titleFactory({
				pathResolutions: node.params,
				valueResolutions: this.#resolvedValues,
			}) ?? {};
			return languageKey ? { key: languageKey, params: translateParams } : null;
		}

		if (typeof data['title'] === 'string') {
			return { key: data['title'] };
		}

		// A path param whose value has a registered display name (e.g. tenant/plan name).
		for (const paramValue of Object.values(node.params ?? {})) {
			const resolved = this.#resolvedValues[paramValue];
			if (resolved) {
				return { key: resolved, literal: true };
			}
		}

		return null;
	}

	#translateKey(tk: TitleKey): string {
		if (!tk?.key) {
			return '';
		}
		if (tk.literal) {
			return tk.key;
		}
		const value = this.translate.instant(tk.key, tk.params);
		// Unknown key -> ngx-translate echoes the key back; drop it rather than show it raw.
		return value === tk.key ? '' : value;
	}

	#clamp(value: string): string {
		if (!value || value.length <= AppTitleStrategy.PART_MAX_LENGTH) {
			return value;
		}
		return value.slice(0, AppTitleStrategy.PART_MAX_LENGTH - 1).trimEnd() + '…';
	}
}
