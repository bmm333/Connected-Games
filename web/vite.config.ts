/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'Partita',
        short_name: 'Partita',
        lang: 'it',
        display: 'standalone',
        background_color: '#000000',
        theme_color: '#000000',
      },
    }),
  ],
  test: {
    environment: 'jsdom',
  },
})
