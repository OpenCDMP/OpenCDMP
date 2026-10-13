import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright config for the accessibility (axe) smoke suite.
 * Specs live in ./e2e-a11y. This is separate from the legacy (EOL) Protractor `e2e/`.
 *
 * Run:  npm run a11y:install   (one-time: downloads Chromium)
 *       npm start             (serve the app on :4200, in another terminal)
 *       npm run test:a11y
 *
 * Authenticated routes need a logged-in session — see e2e-a11y/README.md (storageState).
 */
export default defineConfig({
	testDir: './e2e-a11y',
	timeout: 60_000,
	expect: { timeout: 10_000 },
	retries: 0,
	reporter: [['list'], ['html', { open: 'never' }]],
	use: {
		baseURL: process.env.A11Y_BASE_URL || 'http://localhost:4200',
		headless: true,
		// storageState: 'e2e-a11y/.auth/user.json', // uncomment once auth is set up
	},
	projects: [
		{ name: 'chromium', use: { ...devices['Desktop Chrome'] } },
	],
	// To let Playwright start the app itself in CI, configure a non-interactive serve:
	// webServer: {
	//   command: 'npx ng serve --port 4200',
	//   url: 'http://localhost:4200',
	//   reuseExistingServer: true,
	//   timeout: 180_000,
	// },
});
