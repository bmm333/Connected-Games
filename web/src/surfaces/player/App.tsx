import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client'
import { queryClient } from '@shared/api'
import { PlayerRouter } from './router'
import { getPlayerDependencies } from './composition-root'
import { ThemeProvider } from '@ui'

const dependencies = getPlayerDependencies()

export function App() {
  return (
    <PersistQueryClientProvider
      client={queryClient}
      persistOptions={{ persister: dependencies.queryPersister }}
    >
      <ThemeProvider>
        <PlayerRouter />
      </ThemeProvider>
    </PersistQueryClientProvider>
  )
}

export default App
