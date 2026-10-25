import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client'
import { queryClient } from '@shared/api'
import { ConsoleRouter } from './router'
import { createConsoleDependencies } from './composition-root'
import { ThemeProvider } from '@ui'

const dependencies = createConsoleDependencies()

export function App() {
  return (
    <PersistQueryClientProvider
      client={queryClient}
      persistOptions={{ persister: dependencies.queryPersister }}
    >
      <ThemeProvider>
        <ConsoleRouter />
      </ThemeProvider>
    </PersistQueryClientProvider>
  )
}

export default App
