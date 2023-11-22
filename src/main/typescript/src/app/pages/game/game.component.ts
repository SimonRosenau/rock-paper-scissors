import { CommonModule } from '@angular/common'
import { Component } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { catchError, Observable, of, switchMap } from 'rxjs'

import { Game } from '../../interfaces/game.interface'
import { GameService } from '../../services/game.service'
import { extractErrorMessage } from '../../util/extract-error-message'
import { GameViewComponent } from './components/game-view/game-view.component'

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule, GameViewComponent],
  templateUrl: './game.component.html',
  styleUrl: './game.component.scss',
})
export class GameComponent {
  game$: Observable<Game | null | undefined>
  error: string | null = null

  constructor(
    private route: ActivatedRoute,
    private readonly gameService: GameService
  ) {
    this.game$ = this.route.paramMap.pipe(
      switchMap(params => {
        const gameId = params.get('uuid')
        return gameId ? this.subscribe(gameId) : of(null)
      })
    )
  }

  private subscribe(gameId: string): Observable<Game | null> {
    return this.gameService.subscribe(gameId).pipe(
      catchError(error => {
        this.error = extractErrorMessage(error)
        return of(null)
      })
    )
  }
}
