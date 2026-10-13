/**
 * Lighthouse CI — accessibility-score gate.
 *
 * Run:  npm start            (serve on :4200, another terminal)
 *       npm run lhci
 *
 * In CI, set `startServerCommand` to a non-interactive serve and add authenticated
 * routes once an auth/storage strategy is in place. Threshold starts at 0.9 and
 * should be ratcheted toward 1.0 as issues are burned down.
 */
module.exports = {
	ci: {
		collect: {
			url: [
				'http://localhost:4200/',
				'http://localhost:4200/about',
			],
			numberOfRuns: 1,
			// startServerCommand: 'npx ng serve --port 4200',
			// startServerReadyPattern: 'Local:.*4200',
			settings: { onlyCategories: ['accessibility'] },
		},
		assert: {
			assertions: {
				'categories:accessibility': ['error', { minScore: 0.9 }],
			},
		},
		upload: { target: 'temporary-public-storage' },
	},
};
