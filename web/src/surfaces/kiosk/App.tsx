import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client'
import { queryClient } from '@shared/api'
import { KioskRouter } from './router'
import { createKioskDependencies } from './composition-root'
import { ThemeProvider } from '@ui'

const dependencies = createKioskDependencies()

export function App() {
  return (
    <PersistQueryClientProvider
      client={queryClient}
      persistOptions={{ persister: dependencies.queryPersister }}
    >
      <ThemeProvider>
        <KioskRouter />
      </ThemeProvider>
    </PersistQueryClientProvider>
  )
}

export default App
