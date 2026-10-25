import type { CapacitorConfig } from '@capacitor/cli';

const targetSurface = process.env.CAP_SURFACE || 'player';

const config: CapacitorConfig = {
  appId: 'com.uniupo.ConnectedGames',
  appName: 'ConnectedGames',
  webDir: `dist/${targetSurface}`
};

export default config;
