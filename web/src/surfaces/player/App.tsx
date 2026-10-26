import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client'
import { queryClient } from '@shared/api'
import { PlayerRouter } from './router'
import { createPlayerDependencies } from './composition-root'
import { ThemeProvider } from '@ui'

const dependencies = createPlayerDependencies()

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
