import { HttpInterceptorFn } from '@angular/common/http'
import { inject } from '@angular/core'

import { AuthService } from '../services/auth.service'

/**
 * Interceptor that adds the authorization header to the request.
 * @param req The request.
 * @param next The next interceptor in the chain.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService)
  const value = authService.getAuthorizationHeader()
  if (value && !req.headers.has('Authorization')) {
    const apiReq = req.clone({ setHeaders: { Authorization: value } })
    return next(apiReq)
  }
  return next(req)
}
