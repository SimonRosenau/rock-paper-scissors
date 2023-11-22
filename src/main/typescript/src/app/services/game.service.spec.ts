import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing'
import { TestBed } from '@angular/core/testing'
import { of } from 'rxjs'

import { Game } from '../interfaces/game.interface'
import { GameService } from './game.service'
import { StreamService } from './stream.service'

describe('GameService', () => {
  let service: GameService
  let httpMock: HttpTestingController
  let mockStreamService: jasmine.SpyObj<StreamService>

  beforeEach(() => {
    // Mock the stream service
    mockStreamService = jasmine.createSpyObj('StreamService', ['ndjson'])

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: StreamService, useValue: mockStreamService }],
    })
    service = TestBed.inject(GameService)
    httpMock = TestBed.inject(HttpTestingController)
  })

  afterEach(() => {
    // Verify that there are no outstanding requests
    httpMock.verify()
  })

  it('should be created', () => {
    expect(service).toBeTruthy()
  })

  it('should start a new game', () => {
    const expectedGame = { id: '1' } as Game
    service.startGame().subscribe(game => {
      expect(game).toEqual(expectedGame)
    })

    const req = httpMock.expectOne('/games/start')
    expect(req.request.method).toBe('POST')
    req.flush(expectedGame)
  })

  it('should mark the user as ready', () => {
    const game = { id: '1', players: [{ type: 'user', userId: '1', ready: false, score: 0, selection: null }] } as Game
    const updatedGame: Game = {
      ...game,
      players: game.players.map(player => ({ ...player, ready: true })),
    }
    service.markReady(game).subscribe(updated => {
      expect(updated).toEqual(updatedGame)
    })

    const req = httpMock.expectOne(`/games/${game.id}/ready`)
    expect(req.request.method).toBe('POST')
    req.flush(updatedGame)
  })

  it('should select a hand for the user', () => {
    const game = { id: '1', players: [{ type: 'user', userId: '1', ready: true, score: 0, selection: null }] } as Game
    const hand = Game.Hand.random()
    const updatedGame: Game = {
      ...game,
      players: game.players.map(player => ({ ...player, selection: hand })),
    }
    service.selectHand(game, hand).subscribe(updated => {
      expect(updated).toEqual(updatedGame)
    })
    const req = httpMock.expectOne(`/games/${game.id}/hand`)
    expect(req.request.method).toBe('POST')
    req.flush(updatedGame)
  })

  it('should subscribe to the game stream', () => {
    const gameId = '123'
    const gameUpdates: Game[] = [{ id: '1' } as Game, { id: '2' } as Game, { id: '3' } as Game, { id: '4' } as Game]
    mockStreamService.ndjson.and.returnValue(of(...gameUpdates))

    service.subscribe(gameId).subscribe(update => {
      expect(gameUpdates).toContain(update)
    })
  })
})
