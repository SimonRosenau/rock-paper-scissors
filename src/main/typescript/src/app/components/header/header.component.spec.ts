import { ComponentFixture, TestBed } from '@angular/core/testing'
import { RouterTestingModule } from '@angular/router/testing'
import { BehaviorSubject } from 'rxjs'

import { User } from '../../interfaces/user.interface'
import { AuthService } from '../../services/auth.service'
import { HeaderComponent } from './header.component'

describe('HeaderComponent', () => {
  let component: HeaderComponent
  let fixture: ComponentFixture<HeaderComponent>
  let subject$: BehaviorSubject<User | null | undefined>
  let authServiceMock: jasmine.SpyObj<AuthService>
  let mockUser: User

  beforeEach(async () => {
    // Mock user data
    mockUser = {
      id: '1',
      username: 'Simon',
      createdAt: new Date().toISOString(),
      lastUpdatedAt: new Date().toISOString(),
    }

    subject$ = new BehaviorSubject<User | null | undefined>(undefined)

    // Mock AuthService
    authServiceMock = jasmine.createSpyObj('AuthService', ['getCurrentUser', 'logout'])
    authServiceMock.getCurrentUser.and.returnValue(subject$)
    authServiceMock.logout.and.resolveTo()

    await TestBed.configureTestingModule({
      imports: [HeaderComponent, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    }).compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should start with loading state', done => {
    component.authState$.subscribe(state => {
      expect(state.type).toBe('loading')
      done()
    })
  })

  it('should handle unauthorized state', done => {
    // Mock the return value to simulate unauthenticated user
    subject$.next(null)
    fixture.detectChanges()

    component.authState$.subscribe(state => {
      expect(state.type).toBe('unauthenticated')
      done()
    })
  })

  it('should handle authenticated state', done => {
    // Mock the return value to simulate authenticated user
    subject$.next(mockUser)
    fixture.detectChanges()

    component.authState$.subscribe(state => {
      if (state.type === 'authenticated') {
        expect(state.user).toEqual(mockUser)
        done()
      } else {
        fail('State is not authenticated')
      }
    })
  })

  it('should handle logout process', async () => {
    await component.logout()
    expect(component.isLoggingOut).toBeFalse()
    expect(authServiceMock.logout).toHaveBeenCalled()
  })
})
