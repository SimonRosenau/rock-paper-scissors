import { CommonModule } from '@angular/common'
import { Component, Input } from '@angular/core'

import { LoadingComponent } from '../../../../components/loading/loading.component'
import { Game } from '../../../../interfaces/game.interface'
import { PlaygroundComponent } from '../playground/playground.component'
import { ScoreboardComponent } from '../scoreboard/scoreboard.component'

@Component({
  selector: 'app-game-view',
  standalone: true,
  imports: [CommonModule, ScoreboardComponent, PlaygroundComponent, LoadingComponent],
  templateUrl: './game-view.component.html',
  styleUrl: './game-view.component.scss',
})
export class GameViewComponent {
  @Input({ required: true }) game!: Game
}
