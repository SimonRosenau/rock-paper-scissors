import { CommonModule } from '@angular/common'
import { Component, EventEmitter, Output } from '@angular/core'

import { Game } from '../../../../interfaces/game.interface'

@Component({
  selector: 'app-hand-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './hand-picker.component.html',
  styleUrl: './hand-picker.component.scss',
})
export class HandPickerComponent {
  @Output() handSelected: EventEmitter<Game.Hand> = new EventEmitter<Game.Hand>()
  protected readonly Game = Game
}
