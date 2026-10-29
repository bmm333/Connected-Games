import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client'
import { queryClient } from '@shared/api'
import { ConsoleRouter } from './router'
import { createConsoleDependencies } from './composition-root'
import { ThemeProvider } from '@ui'
import { ConsoleDependenciesContext } from './dependencies'
import { ConsoleAuthProvider } from './auth/ConsoleAuthProvider'

const dependencies = createConsoleDependencies()

export function App() {
  return (
    <PersistQueryClientProvider
      client={queryClient}
      persistOptions={{ persister: dependencies.queryPersister }}
    >
      <ThemeProvider>
        <ConsoleDependenciesContext.Provider value={dependencies}>
          <ConsoleAuthProvider>
            <ConsoleRouter />
          </ConsoleAuthProvider>
        </ConsoleDependenciesContext.Provider>
      </ThemeProvider>
    </PersistQueryClientProvider>
  )
}

export default App
