export interface PlayerScore {
  readonly playerId: string
  readonly points: number
}

export class MatchScore {
  public readonly home: PlayerScore
  public readonly away: PlayerScore

  private constructor(home: PlayerScore, away: PlayerScore) {
    this.home = home
    this.away = away
  }

  public static create(homePlayerId: string, awayPlayerId: string): MatchScore {
    if (!homePlayerId || !awayPlayerId) {
      throw new Error('Player IDs cannot be empty')
    }
    if (homePlayerId === awayPlayerId) {
      throw new Error('A player cannot play against themselves')
    }

    return new MatchScore(
      { playerId: homePlayerId, points: 0 },
      { playerId: awayPlayerId, points: 0 }
    )
  }

  public addPointToHome(): MatchScore {
    return new MatchScore(
      { ...this.home, points: this.home.points + 1 },
      this.away
    )
  }

  public addPointToAway(): MatchScore {
    return new MatchScore(
      this.home,
      { ...this.away, points: this.away.points + 1 }
    )
  }

  public get leader(): string | null {
    if (this.home.points > this.away.points) return this.home.playerId
    if (this.away.points > this.home.points) return this.away.playerId
    return null
  }
}
