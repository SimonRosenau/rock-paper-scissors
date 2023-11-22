import { ComponentFixture, TestBed } from '@angular/core/testing'

import { Game } from '../../../../interfaces/game.interface'
import { ScoreboardComponent } from './scoreboard.component'

describe('ScoreboardComponent', () => {
  let component: ScoreboardComponent
  let fixture: ComponentFixture<ScoreboardComponent>
  let mockGame: Game

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScoreboardComponent],
    }).compileComponents()

    fixture = TestBed.createComponent(ScoreboardComponent)
    component = fixture.componentInstance

    // Mock game data
    mockGame = {
      id: '123',
      createdAt: new Date().toISOString(),
      lastUpdatedAt: new Date().toISOString(),
      state: Game.State.CREATED,
      round: 0,
      players: [],
    }

    // Set the input
    component.game = mockGame

    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })
})
