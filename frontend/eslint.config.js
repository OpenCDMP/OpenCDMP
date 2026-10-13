// @ts-check
// ESLint flat config — replaces the deprecated TSLint + codelyzer setup.
// Focus of this initial config (accessibility Phase 0): restore a working `lint`
// target and turn on the angular-eslint TEMPLATE ACCESSIBILITY rule bundle.
//
// Strategy for a large legacy codebase mid-migration:
//   * The a11y template rules are set to "warn" so the existing backlog is visible
//     and measurable without blocking the whole repo. Ratchet each rule to "error"
//     as it is burned down (see accessibility/IMPLEMENTATION-PLAN.md, Phase 1.6 / 3.5).
//   * lint-staged runs `eslint --max-warnings=0` on STAGED files only, so any file you
//     touch must be clean — new code can never introduce a violation while the legacy
//     backlog is worked off.
//   * Noisy stylistic TS rules are relaxed so the gate stays meaningful for a11y/bugs.

const eslint = require('@eslint/js');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');
const unusedImports = require('eslint-plugin-unused-imports');

module.exports = tseslint.config(
  {
    // Don't lint build output, deps, generated assets, or the audit docs.
    ignores: [
      'dist/**',
      'node_modules/**',
      '.angular/**',
      'coverage/**',
      '**/*.js',
    ],
  },
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    plugins: {
      'unused-imports': unusedImports,
    },
    rules: {
      // Preserve the project's selector conventions (was enforced by codelyzer).
      '@angular-eslint/directive-selector': [
        'error',
        { type: 'attribute', prefix: 'app', style: 'camelCase' },
      ],
      '@angular-eslint/component-selector': [
        'error',
        { type: 'element', prefix: 'app', style: 'kebab-case' },
      ],

      // --- Out of scope for the a11y guardrail: large architectural migrations.
      //     These are tracked separately (see STANDALONE-MIGRATION-ASSESSMENT.md) and
      //     would otherwise bury the a11y/bug signal under thousands of findings. ---
      '@angular-eslint/prefer-standalone': 'off',
      '@angular-eslint/prefer-inject': 'off',
      // The Angular 22 migration pinned every pre-existing component to ChangeDetectionStrategy.Eager.
      '@angular-eslint/prefer-on-push-component-change-detection': 'off',

      // --- Pragmatic relaxations for the legacy TS surface (keep the gate signal-rich,
      //     not a wall of stylistic noise). Tighten over time. ---
      '@typescript-eslint/no-explicit-any': 'off',
      // Use eslint-plugin-unused-imports: `no-unused-imports` is auto-fixable (removes
      // dead imports — the bulk of the backlog); unused locals/params stay as warnings.
      '@typescript-eslint/no-unused-vars': 'off',
      'unused-imports/no-unused-imports': 'warn',
      'unused-imports/no-unused-vars': ['warn', { vars: 'all', varsIgnorePattern: '^_', args: 'none', ignoreRestSiblings: true }],
      '@typescript-eslint/no-empty-function': 'off',
      '@typescript-eslint/no-inferrable-types': 'off',
      '@typescript-eslint/no-this-alias': 'off',
      '@typescript-eslint/no-wrapper-object-types': 'warn',
      '@typescript-eslint/no-empty-object-type': 'warn',
      'no-empty': 'warn',
      'prefer-const': 'warn',
    },
  },
  {
    files: ['**/*.html'],
    extends: [
      ...angular.configs.templateRecommended,
      ...angular.configs.templateAccessibility,
    ],
    rules: {
      // Migration-style template rules (legacy *ngIf/*ngFor, ==): warn, not error,
      // so they don't wall off the a11y signal. Tighten as the templates are modernized.
      '@angular-eslint/template/prefer-control-flow': 'warn',
      // allowNullOrUndefined so the `x == null` idiom (matches null OR undefined) is
      // not auto-changed to `=== null` (which would alter behavior).
      '@angular-eslint/template/eqeqeq': ['warn', { allowNullOrUndefined: true }],

      // === Accessibility rule bundle (WCAG-aligned). "warn" now, ratchet to "error". ===
      // Catches the dominant 775-vs-13 click/key gap and role/label defects from the audit.
      '@angular-eslint/template/click-events-have-key-events': 'warn',
      '@angular-eslint/template/interactive-supports-focus': 'warn',
      '@angular-eslint/template/mouse-events-have-key-events': 'warn',
      '@angular-eslint/template/label-has-associated-control': 'warn',
      '@angular-eslint/template/alt-text': 'warn',
      '@angular-eslint/template/elements-content': 'warn',
      '@angular-eslint/template/no-autofocus': 'warn',
      '@angular-eslint/template/no-distracting-elements': 'warn',
      '@angular-eslint/template/table-scope': 'warn',
      '@angular-eslint/template/valid-aria': 'warn',
      '@angular-eslint/template/role-has-required-aria': 'warn',
    },
  },
);
