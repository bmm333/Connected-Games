/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'
import path from 'node:path'
import { SHARED_ALIASES } from './vite.config.base.ts'

// ─── Path Alias helpers ────────────────────────────────────────────────────
const r = (...parts: string[]) => path.resolve(import.meta.dirname, 'src', ...parts)
const isApiRequest = ({ url }: { url: URL }) =>
  /^\/api\/(?:catalog|games|tournaments|stats)(?:\/|$)/i.test(url.pathname)

/** All feature modules and their Clean Architecture layers */
const MODULES = ['catalog', 'identity', 'match', 'stats', 'tournament'] as const
const LAYERS  = ['domain', 'application', 'adapters', 'ui'] as const

/**
 * Generates one alias entry per module×layer combination.
 * e.g.  '@match/domain' → '<root>/src/modules/match/domain'
 */
const moduleAliases = Object.fromEntries(
  MODULES.flatMap(mod =>
    LAYERS.map(layer => [`@${mod}/${layer}`, r('modules', mod, layer)])
  )
)

// ──────────────────────────────────────────────────────────────────────────
export default defineConfig({
  resolve: {
    alias: {
      ...SHARED_ALIASES,
      // Module layer aliases (auto-generated — add new modules to MODULES above)
      ...moduleAliases,

      // Shared cross-cutting concerns
      '@shared/api':      r('shared', 'api'),
      '@shared/lib':      r('shared', 'lib'),
      '@shared/offline':  r('shared', 'offline'),
      '@shared/realtime': r('shared', 'realtime'),
      '@shared/ui':       r('shared', 'ui'),

      // App shell & surface roots
      '@app':      r('app'),
      '@surfaces': r('surfaces'),
    },
  },

  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: [
        'favicon.ico',
        'favicon.svg',
        'apple-touch-icon-180x180.png',
        'maskable-icon-512x512.png',
        'icons.svg',
      ],
      manifest: {
        id: '/',
        name: 'Partita - Connected Games',
        short_name: 'Partita',
        description: 'Piattaforma di tornei e match gaming in tempo reale',
        lang: 'it',
        start_url: '/',
        scope: '/',
        display: 'standalone',
        orientation: 'portrait-primary',
        background_color: '#0a0a0c',
        theme_color: '#0a0a0c',
        categories: ['games', 'sports', 'entertainment'],
        icons: [
          {
            src: 'pwa-64x64.png',
            sizes: '64x64',
            type: 'image/png',
          },
          {
            src: 'pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png',
          },
          {
            src: 'pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
          },
          {
            src: 'maskable-icon-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable',
          },
        ],
      },
      devOptions: {
        enabled: true,
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        cleanupOutdatedCaches: true,
        navigateFallback: '/index.html',
        navigateFallbackDenylist: [/^\/api\//, /^\/ws\//, /^\/mqtt\//],
        runtimeCaching: [
          // 1. Web Fonts (Google Fonts or CDN)
          {
            urlPattern: /^https:\/\/fonts\.(?:googleapis|gstatic)\.com\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'webfonts-cache',
              cacheableResponse: {
                statuses: [0, 200],
              },
              expiration: {
                maxEntries: 30,
                maxAgeSeconds: 60 * 60 * 24 * 365, // 1 year
              },
            },
          },
          // 2. Static media & images
          {
            urlPattern: /\.(?:png|jpg|jpeg|svg|gif|webp|avif)$/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'images-cache',
              cacheableResponse: {
                statuses: [0, 200],
              },
              expiration: {
                maxEntries: 60,
                maxAgeSeconds: 60 * 60 * 24 * 30, // 30 days
              },
            },
          },
          // 3. API read queries (catalog, tournaments, games metadata)
          {
            urlPattern: isApiRequest,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-read-cache',
              networkTimeoutSeconds: 3,
              cacheableResponse: {
                statuses: [0, 200],
              },
              expiration: {
                maxEntries: 50,
                maxAgeSeconds: 60 * 60 * 24, // 24 hours
              },
            },
          },
        ],
      },
    }),
  ],

  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/**',
        'src/test/**',
        '**/*.d.ts',
        '**/*.config.*',
      ],
    },
  },
})
