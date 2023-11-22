import { CommonModule } from '@angular/common'
import { HttpClientModule } from '@angular/common/http'
import { Component } from '@angular/core'
import { Router } from '@angular/router'
import { firstValueFrom } from 'rxjs'

import { ButtonComponent } from '../../components/button/button.component'
import { GameService } from '../../services/game.service'

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, HttpClientModule, ButtonComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  public loading = false
  constructor(
    private readonly gameService: GameService,
    private readonly router: Router
  ) {}

  async handleGameStart() {
    try {
      this.loading = true
      const game = await firstValueFrom(this.gameService.startGame())
      await this.router.navigateByUrl(`/games/${game.id}`)
    } finally {
      this.loading = false
    }
  }
}
