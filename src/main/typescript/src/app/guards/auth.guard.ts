import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'
import { filter, map, tap } from 'rxjs'

import { AuthService } from '../services/auth.service'

export interface AuthGuardOptions {
  /**
   * The route to navigate to, if the user is not logged in.
   */
  redirectRoute?: string
  /**
   * Reverse the guard, i.e. redirect if the user is logged in.
   */
  reverse?: boolean
}

export const authGuard = (options: AuthGuardOptions = {}): CanActivateFn => {
  const { redirectRoute = '/login', reverse = false } = options
  return _ => {
    const authService = inject(AuthService)
    const router = inject(Router)
    return authService.getCurrentUser().pipe(
      // Filter out undefined, as this means the user profile is not yet loaded.
      filter(user => user !== undefined),
      // Map to true if the user is logged in, false otherwise.
      map(user => user !== null),
      // Map to canActivate, depending on the reverse flag.
      map(isLoggedIn => (reverse ? !isLoggedIn : isLoggedIn)),
      // Redirect if user can not activate the requested route.
      tap(async canActivate => {
        if (!canActivate) {
          await router.navigateByUrl(redirectRoute)
        }
      })
    )
  }
}
