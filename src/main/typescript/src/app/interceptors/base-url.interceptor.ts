import { HttpInterceptorFn } from '@angular/common/http'
import { inject } from '@angular/core'

import { ConfigService } from '../services/config.service'

/**
 * Interceptor that adds the base URL to the request.
 * @param req The request.
 * @param next The next interceptor in the chain.
 */
export const baseUrlInterceptor: HttpInterceptorFn = (req, next) => {
  const config = inject(ConfigService)
  const baseUrl = config.getApiBaseUrl()
  if (!req.url.startsWith('http')) {
    const apiReq = req.clone({ url: `${baseUrl}${req.url}` })
    return next(apiReq)
  }
  return next(req)
}
