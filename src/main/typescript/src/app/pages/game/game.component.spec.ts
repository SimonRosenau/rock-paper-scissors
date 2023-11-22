import { ComponentFixture, TestBed } from '@angular/core/testing'
import { ActivatedRoute, ParamMap } from '@angular/router'
import { RouterTestingModule } from '@angular/router/testing'
import { BehaviorSubject, of, throwError } from 'rxjs'

import { Game } from '../../interfaces/game.interface'
import { GameService } from '../../services/game.service'
import { GameComponent } from './game.component'

describe('GameComponent', () => {
  let component: GameComponent
  let fixture: ComponentFixture<GameComponent>
  let subject$: BehaviorSubject<ParamMap>
  let activatedRouteMock: Pick<ActivatedRoute, 'paramMap'>
  let gameServiceMock: jasmine.SpyObj<GameService>

  beforeEach(async () => {
    subject$ = new BehaviorSubject<ParamMap>({
      has(name: string): boolean {
        return name === 'uuid'
      },
      get(name: string): string | null {
        return name === 'uuid' ? '123' : null
      },
      getAll(name: string): string[] {
        return name === 'uuid' ? ['123'] : []
      },
      keys: ['uuid'],
    })
    activatedRouteMock = {
      paramMap: subject$,
    }
    gameServiceMock = jasmine.createSpyObj('GameService', ['subscribe'])

    await TestBed.configureTestingModule({
      imports: [GameComponent, RouterTestingModule],
      providers: [
        { provide: GameService, useValue: gameServiceMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
    }).compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(GameComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should call gameService.subscribe with the correct id', () => {
    gameServiceMock.subscribe.and.returnValue(of({} as Game))
    expect(gameServiceMock.subscribe).toHaveBeenCalledWith('123')
  })

  it('should handle game data correctly', () => {
    const gameData = { id: '123' } as Game
    gameServiceMock.subscribe.and.returnValue(of(gameData))
    fixture.detectChanges()

    component.game$.subscribe(game => {
      expect(game).toEqual(gameData)
    })
  })

  it('should handle errors correctly', () => {
    const error = new Error('Error')
    gameServiceMock.subscribe.and.returnValue(throwError(() => error))
    fixture.detectChanges()

    component.game$.subscribe(game => {
      expect(game).toBeNull()
      expect(component.error).toBe('Error')
    })
  })
})
