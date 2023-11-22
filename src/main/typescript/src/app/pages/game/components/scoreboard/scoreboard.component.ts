import { CommonModule } from '@angular/common'
import { Component, Input } from '@angular/core'

import { Game } from '../../../../interfaces/game.interface'
import { PlayerComponent } from '../player/player.component'
import { ScoreComponent } from '../score/score.component'

@Component({
  selector: 'app-scoreboard',
  standalone: true,
  imports: [CommonModule, PlayerComponent, ScoreComponent],
  templateUrl: './scoreboard.component.html',
  styleUrl: './scoreboard.component.scss',
})
export class ScoreboardComponent {
  @Input({ required: true }) game!: Game

  public get player(): Game.Player | null {
    return this.game.players.find(player => player.type === 'user') ?? null
  }

  public get opponent(): Game.Player | null {
    return this.game.players.find(player => player.type === 'computer') ?? null
  }
}
