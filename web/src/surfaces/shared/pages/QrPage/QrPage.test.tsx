import { MemoryRouter, useLocation } from 'react-router'
import type { QrScannerRepository } from '@domain/qr/QrScannerRepository'
import { renderWithProviders, screen, waitFor } from '../../../../test/test-utils'
import { QrPage } from './QrPage'

function createScanner(
  scan: QrScannerRepository['scan'],
): QrScannerRepository {
  return {
    isAvailable: vi.fn().mockResolvedValue(true),
    scan,
    stop: vi.fn().mockResolvedValue(undefined),
  }
}

function renderPage(scanner: QrScannerRepository) {
  function LocationProbe() {
    return <span data-testid="location">{useLocation().pathname}</span>
  }

  return renderWithProviders(
    <MemoryRouter>
      <>
        <QrPage
          surface="console"
          scanner={scanner}
          authenticateWithQrCode={vi.fn().mockResolvedValue({
            id: 'session-1',
            kind: 'guest',
            token: { accessToken: 'token' },
            surface: 'console',
            createdAt: '2026-10-25T00:00:00.000Z',
          })}
          pairDevice={vi.fn()}
          destination="/dashboard"
        />
        <LocationProbe />
      </>
    </MemoryRouter>,
  )
}

describe('QrPage', () => {
  it('logs in with a scanned code and redirects to the destination', async () => {
    const scanner = createScanner(vi.fn().mockResolvedValue('scanned-code'))
    const { user } = renderPage(scanner)

    await user.click(await screen.findByRole('button', { name: /inquadra/i }))

    await waitFor(() => {
      expect(screen.getByDisplayValue('scanned-code')).toBeInTheDocument()
    })
    await user.click(screen.getByRole('button', { name: 'Continua' }))

    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/dashboard')
    })
  })

  it('shows a scanner error and keeps the manual fallback available', async () => {
    const scanner = createScanner(
      vi.fn().mockRejectedValue(new Error('Camera permission denied')),
    )
    const { user } = renderPage(scanner)

    await user.click(await screen.findByRole('button', { name: /inquadra/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Camera permission denied',
    )
    expect(screen.getByPlaceholderText('Inserisci il codice')).toBeInTheDocument()
  })
})
