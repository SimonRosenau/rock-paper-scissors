import { HttpInterceptorFn, HttpRequest } from '@angular/common/http'
import { TestBed } from '@angular/core/testing'

import { ConfigService } from '../services/config.service'
import { baseUrlInterceptor } from './base-url.interceptor'

describe('baseUrlInterceptor', () => {
  let configServiceMock: jasmine.SpyObj<ConfigService>
  let nextMock: jasmine.Spy<jasmine.Func>
  const interceptor: HttpInterceptorFn = (req, next) => TestBed.runInInjectionContext(() => baseUrlInterceptor(req, next))

  beforeEach(() => {
    // Mock ConfigService
    configServiceMock = jasmine.createSpyObj('ConfigService', ['getApiBaseUrl'])
    // Mock next interceptor
    nextMock = jasmine.createSpy().and.returnValue({})
    TestBed.configureTestingModule({
      providers: [{ provide: ConfigService, useValue: configServiceMock }],
    })
  })

  it('should be created', () => {
    expect(interceptor).toBeTruthy()
  })

  it('should add base URL to request if not provided', () => {
    const testRequest = new HttpRequest('GET', '/test')
    configServiceMock.getApiBaseUrl.and.returnValue('http://localhost:8080')

    interceptor(testRequest, nextMock)

    expect(nextMock).toHaveBeenCalled()
    const interceptedRequest: Request = nextMock.calls.mostRecent().args[0]
    expect(interceptedRequest.url).toBe('http://localhost:8080/test')
  })
})
