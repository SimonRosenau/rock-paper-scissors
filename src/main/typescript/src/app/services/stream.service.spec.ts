import { HttpClientTestingModule } from '@angular/common/http/testing'
import { TestBed } from '@angular/core/testing'
import { map } from 'rxjs'

import { AuthService } from './auth.service'
import { ConfigService } from './config.service'
import { StreamService } from './stream.service'

describe('StreamService', () => {
  let service: StreamService
  let authServiceMock: jasmine.SpyObj<AuthService>
  let configServiceMock: jasmine.SpyObj<ConfigService>
  let fetchMock: jasmine.Spy<jasmine.Func>

  beforeEach(() => {
    // Mock the auth service
    authServiceMock = jasmine.createSpyObj('AuthService', ['getAuthorizationHeader'])
    // Mock the config service
    configServiceMock = jasmine.createSpyObj('ConfigService', ['getApiBaseUrl'])
    configServiceMock.getApiBaseUrl.and.returnValue('https://test-host/api')
    // Mock fetch
    fetchMock = spyOn(window, 'fetch')

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: ConfigService, useValue: configServiceMock },
      ],
    })
    service = TestBed.inject(StreamService)
  })

  it('should be created', () => {
    expect(service).toBeTruthy()
  })

  it('should make a fetch call', done => {
    const content = JSON.stringify({ test: 'test' })
    const stream: ReadableStream<Uint8Array> = new ReadableStream({
      start(controller) {
        controller.enqueue(new TextEncoder().encode(content))
        controller.close()
      },
    })
    fetchMock.and.returnValue(Promise.resolve(new Response(stream)))
    service.ndjson('/test-path').subscribe({
      complete: () => {
        expect(fetchMock).toHaveBeenCalled()
        done()
      },
    })
  })

  it('should make a fetch call with authorization header', done => {
    const content = JSON.stringify({ test: 'test' })
    const header = 'Bearer test-token'
    const stream: ReadableStream<Uint8Array> = new ReadableStream({
      start(controller) {
        controller.enqueue(new TextEncoder().encode(content))
        controller.close()
      },
    })
    fetchMock.and.returnValue(Promise.resolve(new Response(stream)))
    authServiceMock.getAuthorizationHeader.and.returnValue(header)
    service.ndjson('/test-path').subscribe({
      complete: () => {
        expect(fetchMock).toHaveBeenCalled()
        expect(fetchMock.calls.mostRecent().args[1].headers.get('Authorization')).toEqual(header)
        done()
      },
    })
  })

  it('should make a fetch call to the correct base url', done => {
    const content = JSON.stringify({ test: 'test' })
    const stream: ReadableStream<Uint8Array> = new ReadableStream({
      start(controller) {
        controller.enqueue(new TextEncoder().encode(content))
        controller.close()
      },
    })
    fetchMock.and.returnValue(Promise.resolve(new Response(stream)))
    configServiceMock.getApiBaseUrl.and.returnValue('https://base-url-host.de/api')
    service.ndjson('/test-path').subscribe({
      complete: () => {
        expect(fetchMock).toHaveBeenCalled()
        expect(fetchMock.calls.mostRecent().args[0]).toEqual('https://base-url-host.de/api/test-path')
        done()
      },
    })
  })

  it('should resolve the correct entities', done => {
    const content = new Array(3).fill(null).map((_, index) => ({ test: `test${index + 1}` }))
    const stream: ReadableStream<Uint8Array> = new ReadableStream({
      start(controller) {
        content.forEach(c => controller.enqueue(new TextEncoder().encode(JSON.stringify(c) + '\n')))
        controller.close()
      },
    })
    fetchMock.and.returnValue(Promise.resolve(new Response(stream)))
    service
      .ndjson<{ test: string }>('/test-path')
      .pipe(map((value, index) => ({ value, index })))
      .subscribe({
        next: ({ value, index }) => {
          expect(value).toEqual(content[index])
        },
        complete: () => {
          expect(fetchMock).toHaveBeenCalled()
          done()
        },
      })
  })
})
