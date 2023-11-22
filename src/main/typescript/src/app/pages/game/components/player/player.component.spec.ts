import { CommonModule } from '@angular/common'
import { ComponentFixture, TestBed } from '@angular/core/testing'

import { Game } from '../../../../interfaces/game.interface'
import { PlayerComponent } from './player.component'

describe('PlayerComponent', () => {
  let component: PlayerComponent
  let fixture: ComponentFixture<PlayerComponent>
  let mockUser: Game.Player.User
  let mockComputer: Game.Player.Computer

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule],
    }).compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(PlayerComponent)
    component = fixture.componentInstance

    // Mock player data
    const common = {
      selection: null,
      ready: false,
      score: 0,
    }
    mockUser = {
      type: 'user',
      userId: '1',
      ...common,
    }
    mockComputer = {
      type: 'computer',
      name: 'R2-D2',
      ...common,
    }

    // Set the input
    component.player = mockComputer

    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should display correct name for computer player', () => {
    expect(component.name).toEqual(mockComputer.name)
  })

  it('should display "You" for human player', () => {
    component.player = mockUser
    fixture.detectChanges()
    expect(component.name).toEqual('You')
  })

  it('should display correct icon for computer player', () => {
    expect(component.icon).toEqual('🤖')
  })

  it('should display correct icon for human player', () => {
    component.player = mockUser
    fixture.detectChanges()
    expect(component.icon).toEqual('👤')
  })
})
