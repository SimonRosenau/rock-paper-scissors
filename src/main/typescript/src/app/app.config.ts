import { provideHttpClient, withInterceptors } from '@angular/common/http'
import { ApplicationConfig, ErrorHandler } from '@angular/core'
import { provideRouter } from '@angular/router'

import { routes } from './app.routes'
import { GlobalErrorHandler } from './handlers/global-error.handler'
import { authInterceptor } from './interceptors/auth.interceptor'
import { baseUrlInterceptor } from './interceptors/base-url.interceptor'

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([baseUrlInterceptor, authInterceptor])),
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandler,
    },
  ],
}
