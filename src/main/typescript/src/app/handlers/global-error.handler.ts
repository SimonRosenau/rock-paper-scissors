import { ErrorHandler, Injectable } from '@angular/core'

import { extractErrorMessage } from '../util/extract-error-message'

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor() {}

  handleError(error: any): void {
    const message = extractErrorMessage(error)
    // Only show regular alert for now, could be prettified later
    // eslint-disable-next-line no-alert
    alert(message)
  }
}
