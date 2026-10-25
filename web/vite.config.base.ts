/// <reference types="vitest/config" />
import type { UserConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'
import path from 'node:path'

const r = (...parts: string[]) => path.resolve(import.meta.dirname, 'src', ...parts)
const isApiRequest = ({ url }: { url: URL }) =>
  /^\/api\/(?:catalog|games|tournaments|stats)(?:\/|$)/i.test(url.pathname)

const MODULES = ['catalog', 'identity', 'match', 'stats', 'tournament'] as const
const LAYERS  = ['domain', 'application', 'adapters', 'ui'] as const

const moduleAliases = Object.fromEntries(
  MODULES.flatMap(mod =>
    LAYERS.map(layer => [`@${mod}/${layer}`, r('modules', mod, layer)])
  )
)

export const SHARED_ALIASES = {
  ...moduleAliases,
  '@domain':         r('domain'),
  '@domain/':        r('domain') + path.sep,
  '@use-cases':      r('use-cases'),
  '@use-cases/':     r('use-cases') + path.sep,
  '@infrastructure': r('infrastructure'),
  '@infrastructure/': r('infrastructure') + path.sep,
  '@ui':             r('ui'),
  '@ui/':            r('ui') + path.sep,
  '@shared/api':      r('shared', 'api'),
  '@shared/lib':      r('shared', 'lib'),
  '@shared/offline':  r('shared', 'offline'),
  '@shared/realtime': r('shared', 'realtime'),
  '@shared/ui':       r('shared', 'ui'),
  '@app':             r('app'),
  '@surfaces':        r('surfaces'),
}

export type SurfaceName = 'player' | 'kiosk' | 'console'

interface SurfaceMeta {
  title: string
  shortName: string
  description: string
  port: number
}

const SURFACE_METAS: Record<SurfaceName, SurfaceMeta> = {
  player: {
    title: 'Partita - Player',
    shortName: 'PartitaPlayer',
    description: 'Gaming app per giocatori Connected Games',
    port: 5173,
  },
  kiosk: {
    title: 'Partita - Kiosk & Arena',
    shortName: 'PartitaKiosk',
    description: 'Totem e display arena per Connected Games',
    port: 5174,
  },
  console: {
    title: 'Partita - Admin Console',
    shortName: 'PartitaConsole',
    description: 'Pannello di controllo e gestione tornei',
    port: 5175,
  },
}

export function createSurfaceConfig(surface: SurfaceName): UserConfig {
  const meta = SURFACE_METAS[surface]
  const surfaceRoot = path.resolve(import.meta.dirname, 'src/surfaces', surface)
  const distOutDir = path.resolve(import.meta.dirname, 'dist', surface)
  const publicPath = path.resolve(import.meta.dirname, 'public')

  return {
    root: surfaceRoot,
    publicDir: publicPath,

    resolve: {
      alias: SHARED_ALIASES,
    },

    server: {
      port: meta.port,
    },

    build: {
      outDir: distOutDir,
      emptyOutDir: true,
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
          id: `/${surface}/`,
          name: meta.title,
          short_name: meta.shortName,
          description: meta.description,
          lang: 'it',
          start_url: './',
          scope: './',
          display: 'standalone',
          orientation: surface === 'player' ? 'portrait-primary' : 'any',
          background_color: '#0a0a0c',
          theme_color: '#0a0a0c',
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
        workbox: {
          globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
          cleanupOutdatedCaches: true,
          navigateFallback: 'index.html',
          navigateFallbackDenylist: [/^\/api\//, /^\/ws\//, /^\/mqtt\//],
          runtimeCaching: [
            {
              urlPattern: isApiRequest,
              handler: 'NetworkFirst',
              options: {
                cacheName: `${surface}-api-read-cache`,
                networkTimeoutSeconds: 3,
                cacheableResponse: { statuses: [0, 200] },
                expiration: {
                  maxEntries: 50,
                  maxAgeSeconds: 60 * 60 * 24,
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
      setupFiles: [path.resolve(import.meta.dirname, 'src/test/setup.ts')],
      include: [path.resolve(import.meta.dirname, 'src/**/*.{test,spec}.{ts,tsx}')],
    },
  }
}
