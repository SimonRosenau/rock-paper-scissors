import { CommonModule } from '@angular/common'
import { Component, Input } from '@angular/core'
import { RouterLink } from '@angular/router'

import { LoadingComponent } from '../loading/loading.component'

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingComponent],
  templateUrl: './button.component.html',
  styleUrl: './button.component.scss',
})
export class ButtonComponent {
  @Input() public type: 'button' | 'submit' = 'button'
  @Input() public fullWidth = false
  @Input() public loading = false
}
