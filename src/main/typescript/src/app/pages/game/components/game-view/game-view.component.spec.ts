import { ComponentFixture, TestBed } from '@angular/core/testing'

import { Game } from '../../../../interfaces/game.interface'
import { GameViewComponent } from './game-view.component'

describe('GameViewComponent', () => {
  let component: GameViewComponent
  let fixture: ComponentFixture<GameViewComponent>
  let mockGame: Game

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GameViewComponent],
    }).compileComponents()

    fixture = TestBed.createComponent(GameViewComponent)
    component = fixture.componentInstance

    // Mock game data
    mockGame = {
      id: '1',
      createdAt: new Date().toISOString(),
      lastUpdatedAt: new Date().toISOString(),
      players: [],
      state: Game.State.CREATED,
      round: 0,
    }

    // Set the input
    component.game = mockGame

    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})
