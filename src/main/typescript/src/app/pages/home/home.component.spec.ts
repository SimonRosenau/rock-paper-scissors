import { ComponentFixture, TestBed } from '@angular/core/testing'
import { Router } from '@angular/router'
import { RouterTestingModule } from '@angular/router/testing'
import { of } from 'rxjs'

import { Game } from '../../interfaces/game.interface'
import { GameService } from '../../services/game.service'
import { HomeComponent } from './home.component'

describe('HomeComponent', () => {
  let component: HomeComponent
  let fixture: ComponentFixture<HomeComponent>
  let gameServiceMock: jasmine.SpyObj<GameService>
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
    gameServiceMock = jasmine.createSpyObj('GameService', ['startGame'])
    gameServiceMock.startGame.and.returnValue(of(mockGame))

    await TestBed.configureTestingModule({
      imports: [HomeComponent, RouterTestingModule],
      providers: [{ provide: GameService, useValue: gameServiceMock }],
    }).compileComponents()

    // Get the router
    router = TestBed.inject(Router)
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true))

    fixture = TestBed.createComponent(HomeComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should navigate to the game page', async () => {
    await component.handleGameStart()
    expect(gameServiceMock.startGame).toHaveBeenCalled()
    expect(router.navigateByUrl).toHaveBeenCalledWith(`/games/${mockGame.id}`)
  })
})
