import { CommonModule } from '@angular/common'
import { Component, Input } from '@angular/core'

import { Game } from '../../../../interfaces/game.interface'

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './player.component.html',
  styleUrl: './player.component.scss',
})
export class PlayerComponent {
  @Input({ required: true }) player!: Game.Player

  public get name(): string {
    return this.player.type === 'computer' ? this.player.name : 'You'
  }

  public get icon(): string {
    return this.player.type === 'computer' ? '🤖' : '👤'
  }
}
