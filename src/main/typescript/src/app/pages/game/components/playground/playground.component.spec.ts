import { HttpClientTestingModule } from '@angular/common/http/testing'
import { SimpleChanges } from '@angular/core'
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing'
import { Router } from '@angular/router'
import { RouterTestingModule } from '@angular/router/testing'
import { of } from 'rxjs'

import { Game } from '../../../../interfaces/game.interface'
import { ConfettiService } from '../../../../services/confetti.service'
import { GameService } from '../../../../services/game.service'
import { PlaygroundComponent } from './playground.component'

describe('PlaygroundComponent', () => {
  let component: PlaygroundComponent
  let fixture: ComponentFixture<PlaygroundComponent>
  let gameServiceMock: jasmine.SpyObj<GameService>
  let confettiServiceMock: jasmine.SpyObj<ConfettiService>
  let router: Router
  let mockGame: Game

  beforeEach(async () => {
    // Mock game data
    mockGame = {
      id: '123',
      createdAt: new Date().toISOString(),
      lastUpdatedAt: new Date().toISOString(),
      state: Game.State.CREATED,
      round: 0,
      players: [
        { type: 'user', userId: '123', ready: false, score: 0, selection: null },
        { type: 'computer', name: 'R2-D2', ready: false, score: 0, selection: null },
      ],
    }

    // Mock the game service
    gameServiceMock = jasmine.createSpyObj('GameService', ['markReady', 'selectHand', 'startGame'])
    gameServiceMock.markReady.and.returnValue(of(mockGame))
    gameServiceMock.selectHand.and.returnValue(of(mockGame))
    gameServiceMock.startGame.and.returnValue(of(mockGame))

    // Mock the confetti service
    confettiServiceMock = jasmine.createSpyObj('ConfettiService', ['shoot'])

    await TestBed.configureTestingModule({
      imports: [PlaygroundComponent, HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: GameService, useValue: gameServiceMock },
        { provide: ConfettiService, useValue: confettiServiceMock },
      ],
    }).compileComponents()

    fixture = TestBed.createComponent(PlaygroundComponent)
    component = fixture.componentInstance

    // Get the router
    router = TestBed.inject(Router)
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true))

    // Set the input
    component.game = mockGame

    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should correctly handle ngOnChanges', () => {
    const changes = {
      game: {
        previousValue: { round: 1, state: Game.State.STARTED } as Game,
        currentValue: { round: 2, state: Game.State.STARTED } as Game,
        firstChange: false,
        isFirstChange: () => false,
      },
    } satisfies SimpleChanges

    component.ngOnChanges(changes)
    expect(component.debouncedGame).toEqual(changes.game.previousValue)
  })

  it('should update debouncedGame after timeout', fakeAsync(() => {
    const changes = {
      game: {
        previousValue: { round: 1, state: Game.State.STARTED } as Game,
        currentValue: { round: 2, state: Game.State.STARTED } as Game,
        firstChange: false,
        isFirstChange: () => false,
      },
    } satisfies SimpleChanges

    component.ngOnChanges(changes)
    tick(3000)
    expect(component.debouncedGame).toBeNull()
  }))

  it('should display confetti when user wins', () => {
    const changes = {
      game: {
        previousValue: { state: Game.State.STARTED },
        currentValue: {
          state: Game.State.FINISHED,
          players: [
            { type: 'user', score: 3 },
            { type: 'computer', score: 1 },
          ],
        },
        firstChange: false,
        isFirstChange: () => false,
      },
    } satisfies SimpleChanges

    component.ngOnChanges(changes)
    expect(confettiServiceMock.shoot).toHaveBeenCalled()
  })

  it('should not display confetti when user loses', () => {
    const changes = {
      game: {
        previousValue: { state: Game.State.STARTED },
        currentValue: {
          state: Game.State.FINISHED,
          players: [
            { type: 'user', score: 1 },
            { type: 'computer', score: 3 },
          ],
        },
        firstChange: false,
        isFirstChange: () => false,
      },
    } satisfies SimpleChanges

    component.ngOnChanges(changes)
    expect(confettiServiceMock.shoot).not.toHaveBeenCalled()
  })

  it('should call gameService.markReady when onReady is called', () => {
    component.onReady()
    expect(gameServiceMock.markReady).toHaveBeenCalledWith(component.game)
  })

  it('should call gameService.selectHand when onHand is called', () => {
    const hand = Game.Hand.random()
    component.onHand(hand)
    expect(gameServiceMock.selectHand).toHaveBeenCalledWith(component.game, hand)
  })

  it('should navigate to new game URL on restart', async () => {
    await component.onRestart()
    expect(gameServiceMock.startGame).toHaveBeenCalled()
    expect(router.navigateByUrl).toHaveBeenCalledWith(`/games/${mockGame.id}`)
  })
})
