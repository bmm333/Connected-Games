import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'
import importX from 'eslint-plugin-import-x'
import boundaries from 'eslint-plugin-boundaries'

// ─── Layer / file globs ───────────────────────────────────────────────────────
const DOMAIN_GLOB      = 'src/modules/*/domain/**/*.{ts,tsx}'
const APPLICATION_GLOB = 'src/modules/*/application/**/*.{ts,tsx}'
const ADAPTERS_GLOB    = 'src/modules/*/adapters/**/*.{ts,tsx}'
const GLOBAL_DOMAIN_GLOB = 'src/domain/**/*.{ts,tsx}'
const GLOBAL_APPLICATION_GLOB = 'src/use-cases/**/*.{ts,tsx}'
const GLOBAL_INFRASTRUCTURE_GLOB = 'src/infrastructure/**/*.{ts,tsx}'
const TEST_GLOB        = '**/*.{test,spec}.{ts,tsx}'

// ─── Shared settings (resolver used by both import-x and boundaries) ──────────
const IMPORT_RESOLVER_SETTINGS = {
  'import-x/resolver': {
    typescript: {
      alwaysTryTypes: true,
      project: './tsconfig.app.json',
    },
  },
  'import/resolver': {
    typescript: {
      alwaysTryTypes: true,
      project: './tsconfig.app.json',
    },
  },
}



const BOUNDARY_ELEMENTS = [
  { type: 'domain',      pattern: 'src/modules/*/domain/**'      },
  { type: 'application', pattern: 'src/modules/*/application/**' },
  { type: 'adapters',    pattern: 'src/modules/*/adapters/**'    },
  { type: 'domain',      pattern: 'src/domain/**'                 },
  { type: 'application', pattern: 'src/use-cases/**'              },
  { type: 'adapters',    pattern: 'src/infrastructure/**'         },
  { type: 'ui',          pattern: 'src/modules/*/ui/**'          },
  { type: 'ui',          pattern: 'src/ui/**'                    },
  { type: 'shared',      pattern: 'src/shared/**'                },
  { type: 'app',         pattern: 'src/app/**'                   },
  { type: 'surfaces',    pattern: 'src/surfaces/**'              },
]


export default defineConfig([

  // [0] Global ignores
  globalIgnores(['dist', 'dev-dist', 'android', 'ios', 'node_modules', 'coverage']),

  // [1] Base — all TypeScript / TSX files
  //   Establishes baseline severity for every file in the project.
  {
    name: 'base/ts',
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
    },
    plugins: {
      'import-x': importX,
    },
    settings: IMPORT_RESOLVER_SETTINGS,
    rules: {
      //TypeScript 
      '@typescript-eslint/no-unused-vars': ['warn', {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
      }],
      /** Override per-layer below (domain → error, adapters → warn) */
      '@typescript-eslint/no-explicit-any': 'error',

      /** Enforce `import type` for type-only imports (zero runtime cost) */
      '@typescript-eslint/consistent-type-imports': ['error', {
        prefer: 'type-imports',
        fixStyle: 'inline-type-imports',
      }],

      // ── React ───────────────────────────────────────────────────────────
      'react-hooks/rules-of-hooks':  'error',
      'react-hooks/exhaustive-deps': 'warn',

      // ── Imports (import-x) ──────────────────────────────────────────────
      /** Circular dependencies are an architectural smell → error */
      'import-x/no-cycle':        ['error', { maxDepth: 5 }],
      'import-x/no-duplicates':    'error',
      'import-x/no-self-import':   'error',

      // ── General quality ─────────────────────────────────────────────────
      /** Default warn; tightened to error in domain (see layer [2]) */
      'no-console': 'warn',
    },
  },

  // ── [2] Domain layer — innermost, pure, zero side-effects ─────────────────
  //   The domain must not know about infrastructure, logging, or I/O.
  //   Every rule here is stricter than the base.
  {
    name: 'layer/domain',
    files: [DOMAIN_GLOB],
    rules: {
      /** Domain is a pure functional core — console is forbidden */
      'no-console': 'error',

      /** No escape hatches: domain types must be fully expressed */
      '@typescript-eslint/no-explicit-any': 'error',

      /** Prefer throw new DomainError(...) over untyped throws */
      'no-throw-literal': 'error',
    },
  },
  {
    name: 'layer/global-domain',
    files: [GLOBAL_DOMAIN_GLOB],
    rules: {
      'no-console': 'error',
      '@typescript-eslint/no-explicit-any': 'error',
    },
  },

  // ── [3] Application layer — use cases / orchestration ─────────────────────
  //   Slightly more lenient than domain (logging useful during dev),
  //   still no `any` (use cases must be well-typed).
  {
    name: 'layer/application',
    files: [APPLICATION_GLOB],
    rules: {
      'no-console':                        'warn',
      '@typescript-eslint/no-explicit-any': 'error',
    },
  },
  {
    name: 'layer/global-application',
    files: [GLOBAL_APPLICATION_GLOB],
    rules: {
      'no-console': 'error',
      '@typescript-eslint/no-explicit-any': 'error',
    },
  },

  // ── [4] Adapters layer — infrastructure / I/O ─────────────────────────────
  //   Adapters talk to the outside world (APIs, WebSockets, localStorage).
  //   `any` is downgraded to warn: third-party types are often messy.
  {
    name: 'layer/adapters',
    files: [ADAPTERS_GLOB],
    rules: {
      'no-console':                        'warn',
      /** warn instead of error — adapters may need `any` for SDK interop */
      '@typescript-eslint/no-explicit-any': 'warn',
    },
  },
  {
    name: 'layer/global-infrastructure',
    files: [GLOBAL_INFRASTRUCTURE_GLOB],
    rules: {
      'no-console': 'warn',
      '@typescript-eslint/no-explicit-any': 'warn',
    },
  },

  // ── [5] Test files — most permissive ──────────────────────────────────────
  //   Tests need flexibility: mocking, spying, logging, casting.
  {
    name: 'testing/all',
    files: [TEST_GLOB, 'src/test/**/*.{ts,tsx}'],
    rules: {
      'no-console':                               'off',
      '@typescript-eslint/no-explicit-any':       'off',
      '@typescript-eslint/consistent-type-imports':'off',
      'import-x/no-cycle':                        'off',
      'react-refresh/only-export-components':     'off',
    },
  },

  // ── [6] Config / tooling files — Node environment ─────────────────────────
  {
    name: 'tooling/config',
    files: ['*.config.{js,ts,mjs,cjs}', '*.setup.{js,ts}'],
    languageOptions: {
      globals: { ...globals.browser, ...globals.node },
    },
    rules: {
      'no-console': 'off',
    },
  },

  // ── [7] Clean Architecture boundary enforcement ────────────────────────────
  //   Uses eslint-plugin-boundaries to enforce the Dependency Rule at lint time.
  //   Violations are errors in CI; the architectural diagram above shows what's allowed.
  {
    name: 'architecture/boundaries',
    files: ['src/**/*.{ts,tsx}'],
    plugins: { boundaries },
    settings: {
      ...IMPORT_RESOLVER_SETTINGS,
      'boundaries/elements': BOUNDARY_ELEMENTS,
      /** Tests and static assets are excluded from boundary checks */
      'boundaries/ignore': [TEST_GLOB, '**/*.css', '**/*.svg', '**/*.png', '**/*.ico'],
    },
    rules: {
      /**
       * Dependency Rule (boundaries v7 API) — each layer can only import
       * from layers listed in `allow`. `default: 'disallow'` forbids everything else.
       *
       * Severity matrix:
       *   domain      → shared only
       *   application → domain, shared
       *   adapters    → domain, application, shared   (implements ports)
       *   ui          → domain, application, shared   (NO direct adapters import)
       *   app/surfaces→ everything                    (composition roots)
       *   shared      → shared only                   (self-contained)
       */
      'boundaries/dependencies': ['error', {
        default: 'disallow',
        policies: [
          // shared is self-contained
          {
            from: { element: { type: 'shared' } },
            allow: { to: { element: { type: 'shared' } } },
          },
          // domain: innermost layer — only shared utils
          {
            from: { element: { type: 'domain' } },
            allow: { to: { element: { type: 'shared' } } },
          },
          // application: depends on domain + shared
          {
            from: { element: { type: 'application' } },
            allow: [
              { to: { element: { type: 'domain' } } },
              { to: { element: { type: 'shared' } } },
            ],
          },
          // adapters: implements ports from domain/application
          {
            from: { element: { type: 'adapters' } },
            allow: [
              { to: { element: { type: 'domain' } } },
              { to: { element: { type: 'application' } } },
              { to: { element: { type: 'shared' } } },
            ],
          },
          // ui (presentation): depends on domain + application + shared
          // Does NOT import adapters directly (Dependency Rule)
          {
            from: { element: { type: 'ui' } },
            allow: [
              { to: { element: { type: 'domain' } } },
              { to: { element: { type: 'application' } } },
              { to: { element: { type: 'shared' } } },
            ],
          },
          // app — composition root: can wire everything together
          {
            from: { element: { type: 'app' } },
            allow: [
              { to: { element: { type: 'domain' } } },
              { to: { element: { type: 'application' } } },
              { to: { element: { type: 'adapters' } } },
              { to: { element: { type: 'ui' } } },
              { to: { element: { type: 'shared' } } },
              { to: { element: { type: 'surfaces' } } },
            ],
          },
          // surfaces — composition root: same as app
          {
            from: { element: { type: 'surfaces' } },
            allow: [
              { to: { element: { type: 'domain' } } },
              { to: { element: { type: 'application' } } },
              { to: { element: { type: 'adapters' } } },
              { to: { element: { type: 'ui' } } },
              { to: { element: { type: 'shared' } } },
              { to: { element: { type: 'app' } } },
            ],
          },
        ],
      }],

      /**
       * Warn when a file cannot be classified into any element type.
       * Catches files accidentally placed outside the defined architecture.
       */
      'boundaries/no-unknown-dependencies': 'warn',
    },
  },
])
