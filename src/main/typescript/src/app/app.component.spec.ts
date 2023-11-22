import { TestBed } from '@angular/core/testing'
import { RouterTestingModule } from '@angular/router/testing'
import { of } from 'rxjs'

import { AppComponent } from './app.component'
import { AuthService } from './services/auth.service'

describe('AppComponent', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>
  beforeEach(async () => {
    // Mock AuthService
    authServiceMock = jasmine.createSpyObj('AuthService', ['initialize', 'getCurrentUser'])
    authServiceMock.initialize.and.resolveTo()
    authServiceMock.getCurrentUser.and.returnValue(of(null))

    await TestBed.configureTestingModule({
      imports: [AppComponent, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    }).compileComponents()
  })

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent)
    const app = fixture.componentInstance
    expect(app).toBeTruthy()
  })

  it(`should have the 'Rock Paper Scissors' title`, () => {
    const fixture = TestBed.createComponent(AppComponent)
    const app = fixture.componentInstance
    expect(app.title).toEqual('Rock Paper Scissors')
  })

  it('should initialize the auth service', () => {
    const fixture = TestBed.createComponent(AppComponent)
    fixture.detectChanges()
    expect(authServiceMock.initialize).toHaveBeenCalledTimes(1)
  })
})
