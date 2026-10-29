import { QueryClientProvider } from '@tanstack/react-query'
import { act, render, screen, waitFor } from '@testing-library/react'
import { createTestQueryClient } from '../../../test/test-utils'
import type { ConsoleDependencies } from '../composition-root'
import { ConsoleDependenciesContext } from '../dependencies'
import { useExecuteDeviceCommand } from './useConsoleQueries'

function CommandButton() {
  const command = useExecuteDeviceCommand()
  return (
    <button
      type="button"
      onClick={() => command.mutate({ deviceId: 'device-1', command: 'sync' })}
    >
      Sync
    </button>
  )
}

describe('useExecuteDeviceCommand', () => {
  it('sends the selected command through the injected use case', async () => {
    const executeDeviceCommand = vi.fn().mockResolvedValue(undefined)
    const dependencies = {
      executeDeviceCommand,
    } as unknown as ConsoleDependencies
    const queryClient = createTestQueryClient()

    render(
      <QueryClientProvider client={queryClient}>
        <ConsoleDependenciesContext.Provider value={dependencies}>
          <CommandButton />
        </ConsoleDependenciesContext.Provider>
      </QueryClientProvider>,
    )

    await act(async () => {
      screen.getByRole('button', { name: 'Sync' }).click()
    })

    await waitFor(() => {
      expect(executeDeviceCommand).toHaveBeenCalledWith('device-1', 'sync')
    })
  })
})
