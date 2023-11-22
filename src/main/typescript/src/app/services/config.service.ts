import { Injectable } from '@angular/core'

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  constructor() {}

  /**
   * Returns the base URL of the API
   */
  public getApiBaseUrl(): string {
    // Load dynamically from environment in the future
    return 'http://localhost:8080/api/v1'
  }
}
