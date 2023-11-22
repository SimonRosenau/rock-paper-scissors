import { HttpInterceptorFn, HttpRequest } from '@angular/common/http'
import { TestBed } from '@angular/core/testing'

import { AuthService } from '../services/auth.service'
import { authInterceptor } from './auth.interceptor'

describe('authInterceptor', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>
  let nextMock: jasmine.Spy<jasmine.Func>
  const interceptor: HttpInterceptorFn = (req, next) => TestBed.runInInjectionContext(() => authInterceptor(req, next))

  beforeEach(() => {
    // Mock AuthService
    authServiceMock = jasmine.createSpyObj('AuthService', ['getAuthorizationHeader'])
    // Mock next interceptor
    nextMock = jasmine.createSpy().and.returnValue({})

    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    })
  })

  it('should be created', () => {
    expect(interceptor).toBeTruthy()
  })

  it('should add authorization header if provided by AuthService', () => {
    const testRequest = new HttpRequest('GET', '/test')
    authServiceMock.getAuthorizationHeader.and.returnValue('Bearer token')

    interceptor(testRequest, nextMock)

    expect(nextMock).toHaveBeenCalled()
    const interceptedRequest: HttpRequest<any> = nextMock.calls.mostRecent().args[0]
    expect(interceptedRequest.headers.has('Authorization')).toBeTrue()
    expect(interceptedRequest.headers.get('Authorization')).toBe('Bearer token')
  })

  it('should not modify request if AuthService does not provide authorization header', () => {
    const testRequest = new HttpRequest('GET', '/test')
    authServiceMock.getAuthorizationHeader.and.returnValue(null)

    interceptor(testRequest, nextMock)

    expect(nextMock).toHaveBeenCalledWith(testRequest)
  })
})
