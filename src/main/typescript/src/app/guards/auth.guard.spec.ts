import { TestBed } from '@angular/core/testing'
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router'
import { RouterTestingModule } from '@angular/router/testing'
import { Observable, of, switchMap } from 'rxjs'

import { User } from '../interfaces/user.interface'
import { AuthService } from '../services/auth.service'
import { authGuard } from './auth.guard'

describe('authGuard', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>
  let router: Router

  // Create a mock ActivatedRouteSnapshot
  const mockActivatedRouteSnapshot = {} as ActivatedRouteSnapshot
  const mockRouterStateSnapshot = {} as RouterStateSnapshot

  beforeEach(() => {
    // Mock AuthService and Router
    authServiceMock = jasmine.createSpyObj('AuthService', ['getCurrentUser'])

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [{ provide: AuthService, useValue: authServiceMock }],
    })

    // Get the router
    router = TestBed.inject(Router)
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true))
  })

  it('should allow navigation for authenticated user', done => {
    authServiceMock.getCurrentUser.and.returnValue(of({} as User)) // Mock an authenticated user
    const guard = create()

    call(guard).subscribe(canActivate => {
      expect(canActivate).toBeTrue()
      expect(router.navigateByUrl).not.toHaveBeenCalled()
      done()
    })
  })

  it('should prevent navigation and redirect for unauthenticated user', done => {
    authServiceMock.getCurrentUser.and.returnValue(of(null)) // Mock an unauthenticated user
    const guard = create()

    call(guard).subscribe(canActivate => {
      expect(canActivate).toBeFalse()
      expect(router.navigateByUrl).toHaveBeenCalledWith('/login')
      done()
    })
  })

  it('should allow navigation for unauthenticated user when reverse is true', done => {
    authServiceMock.getCurrentUser.and.returnValue(of(null)) // Mock an unauthenticated user
    const guard = create({ reverse: true })

    call(guard).subscribe(canActivate => {
      expect(canActivate).toBeTrue()
      expect(router.navigateByUrl).not.toHaveBeenCalled()
      done()
    })
  })

  /**
   * Helper function to create a guard with the given options within the TestBed's injection context.
   * @param options The options to pass to the guard.
   * @returns The created guard.
   */
  const create: typeof authGuard = options => {
    const guard = authGuard(options)
    return (route, state) => TestBed.runInInjectionContext(() => guard(route, state))
  }

  /**
   * Helper function to call a guard and return its return value as an Observable.
   * @param guard The guard to call.
   * @returns An Observable of the guard's return value.
   */
  const call = (guard: CanActivateFn) => {
    return of(guard(mockActivatedRouteSnapshot, mockRouterStateSnapshot)).pipe(
      // Ensure the guard's return value is an Observable before subscribing
      switchMap(result => (result instanceof Observable ? result : of(result)))
    )
  }
})
