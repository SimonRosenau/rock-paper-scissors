import { CommonModule } from '@angular/common'
import { Component } from '@angular/core'
import { RouterLink } from '@angular/router'
import { map } from 'rxjs'

import { User } from '../../interfaces/user.interface'
import { AuthService } from '../../services/auth.service'
import { ButtonComponent } from '../button/button.component'
import { LogoComponent } from '../logo/logo.component'

type UserState =
  | {
      type: 'loading'
    }
  | {
      type: 'unauthenticated'
    }
  | {
      type: 'authenticated'
      user: User
    }

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, LogoComponent, ButtonComponent, RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {
  public authState$ = this.authService.getCurrentUser().pipe(
    map<User | null | undefined, UserState>(user => {
      if (user === undefined) {
        return { type: 'loading' } as UserState
      } else if (user === null) {
        return { type: 'unauthenticated' } as UserState
      } else {
        return { type: 'authenticated', user } as UserState
      }
    })
  )
  public isLoggingOut = false

  constructor(private readonly authService: AuthService) {}

  async logout() {
    try {
      this.isLoggingOut = true
      await this.authService.logout()
    } finally {
      this.isLoggingOut = false
    }
  }
}
