import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing'
import { TestBed } from '@angular/core/testing'
import { Router } from '@angular/router'
import { RouterTestingModule } from '@angular/router/testing'
import { filter } from 'rxjs'

import { User } from '../interfaces/user.interface'
import { AuthService } from './auth.service'
import { StorageService } from './storage.service'

describe('AuthService', () => {
  const STORAGE_KEY = 'auth.token'
  let httpTestingController: HttpTestingController
  let storageServiceMock: jasmine.SpyObj<StorageService>
  let router: Router
  let service: AuthService

  beforeEach(() => {
    // Mock storage service
    storageServiceMock = jasmine.createSpyObj('StorageService', ['get', 'set', 'remove'])

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService, { provide: StorageService, useValue: storageServiceMock }],
    })

    // Get the router
    router = TestBed.inject(Router)
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true))

    httpTestingController = TestBed.inject(HttpTestingController)
    service = TestBed.inject(AuthService)
  })

  afterEach(() => {
    httpTestingController.verify()
  })

  it('should be created', () => {
    expect(service).toBeTruthy()
  })

  it('should initialize correctly without token', () => {
    spyOn(service, 'initialize').and.callThrough()
    service.initialize()
    expect(service.initialize).toHaveBeenCalled()
    service.isLoggedIn().subscribe(isLoggedIn => {
      expect(isLoggedIn).toBeFalse()
    })
  })

  it('should initialize correctly with token', () => {
    const testToken = 'someEncodedToken'
    const testUser = { id: '1' } as User

    // Mock the get method of StorageService to return the test token
    storageServiceMock.get.and.returnValue(testToken)

    // Call the initialize method
    service.initialize()

    // Expect that a request is made to fetch the user profile
    const req = httpTestingController.expectOne({ method: 'GET', url: '/users/profile' })
    expect(req.request.headers.has('Authorization')).toBeTrue()
    expect(req.request.headers.get('Authorization')).toBe('Basic ' + testToken)

    // Respond with the mock user
    req.flush(testUser)

    // Check if the currentUser Observable is updated with the fetched user
    service
      .getCurrentUser()
      .pipe(filter(user => !!user))
      .subscribe(user => {
        expect(user).toEqual(testUser)
      })
  })

  it('should login and store token', () => {
    const testUser = { id: '1' } as User
    const username = 'test'
    const password = 'password'

    service.login(username, password).subscribe(user => {
      expect(user).toEqual(testUser)
    })

    const req = httpTestingController.expectOne('/users/profile')
    expect(req.request.method).toEqual('GET')
    req.flush(testUser)

    expect(storageServiceMock.set).toHaveBeenCalledWith(STORAGE_KEY, jasmine.any(String))

    service.getCurrentUser().subscribe(user => {
      expect(user).toEqual(testUser)
    })
  })

  it('should logout and remove token', async () => {
    await service.logout()
    expect(storageServiceMock.remove).toHaveBeenCalledWith(STORAGE_KEY)
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login')

    service.getCurrentUser().subscribe(user => {
      expect(user).toBeNull()
    })
  })

  it('should register a new user', () => {
    const testUser = { id: '1' } as User
    const username = 'newUser'
    const password = 'newPassword'

    service.register(username, password).subscribe(user => {
      expect(user).toEqual(testUser)
    })

    const req = httpTestingController.expectOne('/users')
    expect(req.request.method).toEqual('POST')
    req.flush(testUser)

    expect(storageServiceMock.set).toHaveBeenCalledWith(STORAGE_KEY, jasmine.any(String))

    service.getCurrentUser().subscribe(user => {
      expect(user).toEqual(testUser)
    })
  })
})
