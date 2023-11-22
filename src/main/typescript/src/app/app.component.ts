import { CommonModule } from '@angular/common'
import { Component, OnInit } from '@angular/core'
import { RouterLink, RouterOutlet } from '@angular/router'

import { HeaderComponent } from './components/header/header.component'
import { LogoComponent } from './components/logo/logo.component'
import { AuthService } from './services/auth.service'

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, LogoComponent, HeaderComponent, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  title = 'Rock Paper Scissors'

  constructor(private readonly authService: AuthService) {}

  ngOnInit(): void {
    this.authService.initialize()
  }
}
