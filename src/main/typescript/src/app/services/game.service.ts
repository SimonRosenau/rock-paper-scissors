import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { filter, merge, Observable, Subject } from 'rxjs'

import { SelectHandDto } from '../interfaces/dto/select-hand.dto'
import { Game } from '../interfaces/game.interface'
import { StreamService } from './stream.service'

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private readonly optimisticGames$ = new Subject<Game>()
  constructor(
    private readonly http: HttpClient,
    private readonly streamService: StreamService
  ) {}

  /**
   * Starts a new game.
   * @return An observable that emits the new game.
   */
  public startGame(): Observable<Game> {
    return this.http.post<Game>('/games/start', null)
  }

  /**
   * Marks the user as ready.
   * @param game The game to mark the user as ready for.
   * @return An observable that emits the updated game.
   */
  public markReady(game: string | Game): Observable<Game> {
    const gameId = typeof game === 'object' ? game.id : game
    this.publishOptimistically(game, game => ({
      ...game,
      players: game.players.map(p => {
        return p.type === 'user' ? { ...p, ready: true } : p
      }),
    }))
    return this.http.post<Game>(`/games/${gameId}/ready`, null)
  }

  /**
   * Selects a hand for the user.
   * @param game The game to select the hand for.
   * @param hand The hand to select.
   * @return An observable that emits the updated game.
   */
  public selectHand(game: string | Game, hand: Game.Hand): Observable<Game> {
    const gameId = typeof game === 'object' ? game.id : game
    this.publishOptimistically(game, game => ({
      ...game,
      players: game.players.map(p => {
        return p.type === 'user' ? { ...p, selection: hand } : p
      }),
    }))
    return this.http.post<Game>(`/games/${gameId}/hand`, { hand } satisfies SelectHandDto)
  }

  /**
   * Subscribes to the game stream.
   * @param gameId The id of the game to subscribe to.
   * @return An observable that emits the game updates.
   */
  public subscribe(gameId: string): Observable<Game> {
    const subscription$ = this.streamService.ndjson<Game>(`/games/${gameId}/stream`)
    const optimistic$ = this.optimisticGames$.asObservable().pipe(filter(game => game.id === gameId))
    return merge(subscription$, optimistic$)
  }

  /**
   * Publishes an optimistic update to the game.
   * This is a basic implementation and can theoretically cause race-conditions.
   * A more robust implementation would use some sort of overlay cache,
   *    to apply optimistic patches to all incoming games, until the server confirms the update.
   *
   * The race-condition can be caused by the following scenario:
   * 1. a) User A selects a hand
   *    b) Backend simultaneously selects a hand for the opponent
   * 2. Optimistic update is emitted
   * 3. Backend emits the game with the opponent's hand, overriding the optimistic update
   * 4. Backend handles the hand selection of User A and emits the game with the user's hand
   *
   * Depending on the timing of the events, there could occur a flickering effect, between numbers 3 and 4.
   *
   * @param game The game to update (will ignore gameIds)
   * @param fn The function to apply to the game
   */
  private publishOptimistically(game: string | Game, fn: (game: Game) => Game): void {
    if (typeof game === 'object') {
      this.optimisticGames$.next(fn(game))
    }
  }
}
