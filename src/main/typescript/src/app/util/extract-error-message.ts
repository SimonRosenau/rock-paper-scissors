import { HttpErrorResponse } from '@angular/common/http'

import { ErrorDto } from '../interfaces/dto/error.dto'

/**
 * Extracts the error message from the given error.
 * @param error The error to extract the message from.
 * @return The error message.
 */
export function extractErrorMessage(error: any) {
  if (error instanceof Error) {
    return error.message
  } else if (typeof error === 'string') {
    return error
  } else if (error instanceof HttpErrorResponse) {
    return ErrorDto.is(error.error) ? error.error.error : error.message
  } else if (ErrorDto.is(error)) {
    return error.error
  } else if (error instanceof Object) {
    return error.message || JSON.stringify(error)
  } else {
    return 'Unknown error'
  }
}
