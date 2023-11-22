import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { Router } from '@angular/router'
import { BehaviorSubject, firstValueFrom, map, Observable, tap } from 'rxjs'

import { CreateUserDto } from '../interfaces/dto/create-user.dto'
import { User } from '../interfaces/user.interface'
import { StorageService } from './storage.service'

/**
 * The authentication service.
 * NOTE:  Basic authentication is used for simplicity.
 *        This is not secure and should not be used in a serious production environment.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private static readonly STORAGE_KEY = 'auth.token'
  private currentUser$ = new BehaviorSubject<User | null | undefined>(undefined)

  constructor(
    private readonly http: HttpClient,
    private readonly storageService: StorageService,
    private readonly router: Router
  ) {}

  /**
   * Initialize the authentication context and retrieve the current user profile.
   */
  initialize() {
    const token = this.storageService.get(AuthService.STORAGE_KEY)
    if (token) {
      const headers = { Authorization: 'Basic ' + token }
      firstValueFrom(this.http.get<User>('/users/profile', { headers }))
        .then(user => this.currentUser$.next(user))
        .catch(() => this.currentUser$.next(null))
    } else {
      this.currentUser$.next(null)
    }
  }

  /**
   * Returns the current user profile.
   * @return The current user profile.
   *     null: The user is not logged in.
   *     undefined: The user profile is not yet loaded.
   */
  public getCurrentUser(): Observable<User | null | undefined> {
    return this.currentUser$.asObservable()
  }

  /**
   * Returns true if the user is logged in, false otherwise.
   */
  public isLoggedIn(): Observable<boolean> {
    return this.currentUser$.pipe(map(user => !!user))
  }

  /**
   * Logs the user out.
   */
  public async logout(): Promise<void> {
    this.storageService.remove(AuthService.STORAGE_KEY)
    this.currentUser$.next(null)
    await this.router.navigateByUrl('/login')
  }

  /**
   * Logs the user in and stores the credentials in the app storage.
   * @param username The username.
   * @param password The password.
   * @return The logged-in user.
   */
  public login(username: string, password: string): Observable<User> {
    const token = this.createBasicAuthorizationToken(username, password)
    const headers = { Authorization: `Basic ${token}` }
    return this.http.get<User>('/users/profile', { headers }).pipe(
      tap(user => {
        this.storageService.set(AuthService.STORAGE_KEY, token)
        this.currentUser$.next(user)
      })
    )
  }

  /**
   * Registers the user, logs the user in and stores the credentials in the app storage.
   * @param username The username.
   * @param password The password.
   * @return The registered and logged-in user.
   */
  public register(username: string, password: string): Observable<User> {
    return this.http.post<User>('/users', { username, password } satisfies CreateUserDto).pipe(
      tap(user => {
        this.storageService.set(AuthService.STORAGE_KEY, this.createBasicAuthorizationToken(username, password))
        this.currentUser$.next(user)
      })
    )
  }

  /**
   * Retrieves the current logged-in user profile.
   */
  public getProfile(): Observable<User> {
    return this.http.get<User>('/users/profile')
  }

  /**
   * Retrieves the current authorization header.
   */
  public getAuthorizationHeader(): string | null {
    const token = this.storageService.get(AuthService.STORAGE_KEY)
    return token ? 'Basic ' + token : null
  }

  /**
   * Creates a basic authorization token.
   * NOTE:  Basic authentication is used for simplicity.
   *        This is not secure and should not be used in a serious production environment.
   * @param username The username.
   * @param password The password.
   * @return The basic authorization header.
   */
  private createBasicAuthorizationToken(username: string, password: string): string {
    return btoa(username + ':' + password)
  }
}
