import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

/**
 * Axe-core accessibility smoke scan over key routes.
 *
 * - Scans against WCAG 2.0/2.1/2.2 A + AA rule tags.
 * - BUDGET: fails the test only on `critical`/`serious` violations (tighten to all
 *   over time). All violations are logged for visibility.
 * - Override the route list:  A11Y_ROUTES="/,/about,/plans"  npm run test:a11y
 * - Authenticated routes (plan editor, admin, etc.) require a logged-in session;
 *   set up storageState (see e2e-a11y/README.md) then add those routes here.
 */
const ROUTES = (process.env.A11Y_ROUTES || '/,/about,/plans,/descriptions')
	.split(',')
	.map(r => r.trim())
	.filter(Boolean);

const WCAG_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'];

for (const route of ROUTES) {
	test(`a11y smoke: ${route}`, async ({ page }) => {
		await page.goto(route, { waitUntil: 'networkidle' });

		const results = await new AxeBuilder({ page }).withTags(WCAG_TAGS).analyze();

		if (results.violations.length) {
			const lines = results.violations.map(
				v => `  [${v.impact}] ${v.id}: ${v.help} — ${v.nodes.length} node(s)`,
			);
			console.log(`\n[${route}] axe violations (${results.violations.length}):\n${lines.join('\n')}`);
		}

		const blocking = results.violations.filter(
			v => v.impact === 'critical' || v.impact === 'serious',
		);
		expect(
			blocking,
			`critical/serious axe violations on ${route}:\n` +
				blocking.map(v => `${v.id} — ${v.help}`).join('\n'),
		).toEqual([]);
	});
}
