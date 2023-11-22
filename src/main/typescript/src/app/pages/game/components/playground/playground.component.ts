import { CommonModule } from '@angular/common'
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core'
import { Router } from '@angular/router'
import { firstValueFrom } from 'rxjs'

import { ButtonComponent } from '../../../../components/button/button.component'
import { Game } from '../../../../interfaces/game.interface'
import { ConfettiService } from '../../../../services/confetti.service'
import { GameService } from '../../../../services/game.service'
import { HandPickerComponent } from '../hand-picker/hand-picker.component'

@Component({
  selector: 'app-playground',
  standalone: true,
  imports: [CommonModule, ButtonComponent, HandPickerComponent],
  templateUrl: './playground.component.html',
  styleUrl: './playground.component.scss',
})
export class PlaygroundComponent implements OnChanges {
  @Input({ required: true }) game!: Game
  public debouncedGame: Game | null = null

  public isRestarting: boolean = false

  constructor(
    private readonly gameService: GameService,
    private readonly confettiService: ConfettiService,
    private readonly router: Router
  ) {}

  /**
   * Catch game changes and debounce the round switches
   * to let the user see and understand the
   * outcome before switching to the next round.
   * @param changes
   */
  ngOnChanges(changes: SimpleChanges): void {
    const gameChange = changes['game']
    if (gameChange) {
      const prev: Game | null = gameChange.previousValue
      const next: Game | null = gameChange.currentValue

      // Catch game changes and debounce the round switches
      // to let the user see and understand the
      // outcome before switching to the next round.
      //   -> only if the game is not in the first round
      if (prev && next && prev.round !== 0 && prev.round !== next.round) {
        this.debouncedGame = prev
        setTimeout(() => {
          this.debouncedGame = null
        }, 3000)
      }

      // Catch game finished changes and show confetti if the user won.
      if (prev && next && prev.state !== Game.State.FINISHED && next.state === Game.State.FINISHED) {
        const userScore = next.players.find(p => p.type === 'user')?.score ?? 0
        const computerScore = next.players.find(p => p.type === 'computer')?.score ?? 0
        if (userScore > computerScore) {
          this.confettiService.shoot()
        }
      }
    }
  }

  /**
   * Returns the current game state to render (respecting the debounced state).
   */
  public get current(): Game {
    return this.debouncedGame ?? this.game
  }

  public get player(): Game.Player.User {
    return this.current.players.find(p => p.type === 'user') as Game.Player.User
  }

  public get opponent(): Game.Player.Computer {
    return this.current.players.find(p => p.type === 'computer') as Game.Player.Computer
  }

  public get statusText(): string {
    switch (this.current.state) {
      case Game.State.CREATED:
        return 'Waiting for players to get ready...'
      case Game.State.STARTED:
        return `Round ${this.current.round}`
      case Game.State.FINISHED:
        return 'Game finished!'
      case Game.State.CANCELLED:
        return 'Game cancelled!'
    }
  }

  public get outcomeText(): string {
    switch (this.current.state) {
      case Game.State.CREATED:
      case Game.State.STARTED:
        return ''
      case Game.State.FINISHED:
        const maxScore = Math.max(...this.current.players.map(p => p.score))
        const winners = this.current.players.filter(p => p.score === maxScore)
        if (winners.length === 1) {
          const winner = winners[0]
          return winner.type === 'user' ? 'You win!' : `${winner.name} wins!`
        } else {
          return "It's a tie!"
        }
      case Game.State.CANCELLED:
        return 'Game cancelled!'
    }
  }

  // Controls

  public onReady(): void {
    this.gameService.markReady(this.game).subscribe()
  }

  public onHand(hand: Game.Hand): void {
    this.gameService.selectHand(this.game, hand).subscribe()
  }

  public async onRestart(): Promise<void> {
    try {
      this.isRestarting = true
      const game = await firstValueFrom(this.gameService.startGame())
      await this.router.navigateByUrl(`/games/${game.id}`)
    } finally {
      this.isRestarting = false
    }
  }

  protected readonly Game = Game
}
