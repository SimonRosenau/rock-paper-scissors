import { Injectable } from '@angular/core'
import { Observable } from 'rxjs'

import { AuthService } from './auth.service'
import { ConfigService } from './config.service'

@Injectable({
  providedIn: 'root',
})
export class StreamService {
  constructor(
    private readonly config: ConfigService,
    private readonly authService: AuthService
  ) {}

  /**
   * Subscribes to live updates from the server using Server-Sent Events
   * @param path The path to subscribe to
   * @returns An observable that emits the data from the server
   */
  public ndjson<T>(path: string): Observable<T> {
    return new Observable<T>(observer => {
      // Initialize headers
      const headers = new Headers()
      headers.append('Accept', 'application/x-ndjson')

      // Construct headers with authorization token
      const token = this.authService.getAuthorizationHeader()
      if (token) headers.append('Authorization', token)

      // Utilize the AbortController to abort the request when the observable is unsubscribed
      const abortController = new AbortController()

      // Perform the request
      fetch(this.config.getApiBaseUrl() + path, { headers, signal: abortController.signal })
        .then(async response => {
          if (response.ok && response.body) {
            const byteStream: ReadableStream<Uint8Array> = response.body
            return this.readNdjsonStream(byteStream, observer.next.bind(observer))
          } else if (response.body) {
            // Parse error response as text and throw it as ErrorDto
            const errorText = await response.text()
            throw {
              timestamp: new Date().toISOString(),
              status: response.status,
              error: errorText,
              path: path,
            }
          } else {
            throw new Error('Response body is null')
          }
        })
        .then(() => observer.complete())
        .catch(error => observer.error(error))

      return () => {
        abortController.abort()
      }
    })
  }

  /**
   * Reads the given stream line-by-line and emits the data to the given callback.
   * @param stream The stream to read.
   * @param emit The callback to emit the data to.
   * @private This method is private and should not be used outside of this class.
   */
  private async readNdjsonStream<T>(stream: ReadableStream<Uint8Array>, emit: (next: T) => void): Promise<void> {
    const reader = stream.getReader()
    const textDecoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const data = await reader.read()
      if (data.done) {
        break
      }
      buffer += textDecoder.decode(data.value)
      // Once at least one line is fully read, parse and emit the entity/entities
      if (buffer.includes('\n')) {
        // Split the buffer into lines
        const lines = buffer.split('\n')
        // Reset the buffer to the last incomplete line
        buffer = lines.pop() as string
        // Parse and emit the entities
        for (const entity of lines.map(line => JSON.parse(line))) {
          emit(entity)
        }
      }
    }
  }
}
