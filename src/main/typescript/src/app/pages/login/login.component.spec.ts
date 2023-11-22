import { ComponentFixture, TestBed } from '@angular/core/testing'
import { Router } from '@angular/router'
import { RouterTestingModule } from '@angular/router/testing'
import { of, throwError } from 'rxjs'

import { User } from '../../interfaces/user.interface'
import { AuthService } from '../../services/auth.service'
import { LoginComponent } from './login.component'

describe('LoginComponent', () => {
  let component: LoginComponent
  let fixture: ComponentFixture<LoginComponent>
  let authServiceMock: jasmine.SpyObj<AuthService>
  let router: Router

  beforeEach(async () => {
    // Mock AuthService
    authServiceMock = jasmine.createSpyObj('AuthService', ['login'])
    authServiceMock.login.and.resolveTo()

    await TestBed.configureTestingModule({
      imports: [LoginComponent, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    }).compileComponents()

    // Get the router
    router = TestBed.inject(Router)

    fixture = TestBed.createComponent(LoginComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should initialize form with empty fields', () => {
    expect(component.loginForm.value).toEqual({ username: '', password: '' })
  })

  it('should not call AuthService login if form is invalid', () => {
    component.onSubmit()
    expect(authServiceMock.login).not.toHaveBeenCalled()
  })

  it('should call AuthService login if form is valid', () => {
    authServiceMock.login.and.returnValue(of({} as User))
    component.loginForm.setValue({ username: 'testuser', password: 'password' })
    component.onSubmit()
    expect(authServiceMock.login).toHaveBeenCalledWith('testuser', 'password')
  })

  it('should navigate to home on successful login', async () => {
    authServiceMock.login.and.returnValue(of({} as User))
    const navigateSpy = spyOn(router, 'navigateByUrl')
    component.loginForm.setValue({ username: 'testuser', password: 'password' })
    await component.onSubmit()
    expect(navigateSpy).toHaveBeenCalledWith('/')
  })

  it('should handle login errors', async () => {
    const error = new Error('Login failed')
    authServiceMock.login.and.returnValue(throwError(() => error))
    component.loginForm.setValue({ username: 'testuser', password: 'password' })
    await component.onSubmit()
    expect(component.loginForm.errors).toEqual({ unknownError: 'Login failed' })
  })
})
