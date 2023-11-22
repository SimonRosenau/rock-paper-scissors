import { ComponentFixture, TestBed } from '@angular/core/testing'
import { Router } from '@angular/router'
import { RouterTestingModule } from '@angular/router/testing'
import { of, throwError } from 'rxjs'

import { User } from '../../interfaces/user.interface'
import { AuthService } from '../../services/auth.service'
import { RegisterComponent } from './register.component'

describe('RegisterComponent', () => {
  let component: RegisterComponent
  let fixture: ComponentFixture<RegisterComponent>
  let authServiceMock: jasmine.SpyObj<AuthService>
  let router: Router

  beforeEach(async () => {
    // Mock AuthService
    authServiceMock = jasmine.createSpyObj('AuthService', ['register'])
    authServiceMock.register.and.resolveTo()

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    }).compileComponents()

    // Get the router
    router = TestBed.inject(Router)

    fixture = TestBed.createComponent(RegisterComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('should initialize form with empty fields', () => {
    expect(component.registerForm.value).toEqual({
      username: '',
      password: '',
      confirmPassword: '',
    })
  })

  it('should validate password match', () => {
    const form = component.registerForm
    form.setValue({ username: 'testuser', password: 'Password123', confirmPassword: 'Password1234' })
    expect(form.hasError('passwordMismatch')).toBeTrue()
  })

  it('should not call AuthService register if form is invalid', () => {
    component.onSubmit()
    expect(authServiceMock.register).not.toHaveBeenCalled()
  })

  it('should call AuthService register if form is valid', () => {
    authServiceMock.register.and.returnValue(of({} as User))
    component.registerForm.setValue({ username: 'testuser', password: 'Password123', confirmPassword: 'Password123' })
    component.onSubmit()
    expect(authServiceMock.register).toHaveBeenCalledWith('testuser', 'Password123')
  })

  it('should navigate to home on successful registration', async () => {
    authServiceMock.register.and.returnValue(of({} as User))
    const navigateSpy = spyOn(router, 'navigateByUrl')
    component.registerForm.setValue({ username: 'testuser', password: 'Password123', confirmPassword: 'Password123' })
    await component.onSubmit()
    expect(navigateSpy).toHaveBeenCalledWith('/')
  })

  it('should handle registration errors', async () => {
    const error = new Error('Username already exists')
    authServiceMock.register.and.returnValue(throwError(() => error))
    component.registerForm.setValue({ username: 'testuser', password: 'Password123', confirmPassword: 'Password123' })
    await component.onSubmit()
    expect(component.registerForm.errors).toEqual({ usernameTaken: true })
  })
})
