import { describe, it, expect } from 'vitest'
import { renderWithProviders, screen } from '../../../test/test-utils'
import { ScoreBoard } from '@match/ui/ScoreBoard'

describe('ScoreBoard Component (Integration / UI Test)', () => {
  it('renders initial scores and tie status', () => {
    renderWithProviders(
      <ScoreBoard initialHomePlayer="Mario" initialAwayPlayer="Luigi" />
    )

    expect(screen.getByText('Punteggio Partita')).toBeInTheDocument()
    expect(screen.getByText('Mario')).toBeInTheDocument()
    expect(screen.getByText('Luigi')).toBeInTheDocument()
    expect(screen.getByTestId('home-points')).toHaveTextContent('0')
    expect(screen.getByTestId('away-points')).toHaveTextContent('0')
    expect(screen.getByTestId('current-leader')).toHaveTextContent('Pareggio')
  })

  it('updates points and leader when buttons are clicked', async () => {
    const { user } = renderWithProviders(
      <ScoreBoard initialHomePlayer="Mario" initialAwayPlayer="Luigi" />
    )

    const buttons = screen.getAllByRole('button', { name: '+1 Punto' })
    const homeAddButton = buttons[0]
    const awayAddButton = buttons[1]

    await user.click(homeAddButton)

    expect(screen.getByTestId('home-points')).toHaveTextContent('1')
    expect(screen.getByTestId('away-points')).toHaveTextContent('0')
    expect(screen.getByTestId('current-leader')).toHaveTextContent('Mario')

    await user.click(awayAddButton)
    await user.click(awayAddButton)

    expect(screen.getByTestId('home-points')).toHaveTextContent('1')
    expect(screen.getByTestId('away-points')).toHaveTextContent('2')
    expect(screen.getByTestId('current-leader')).toHaveTextContent('Luigi')
  })
})
