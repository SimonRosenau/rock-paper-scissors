import { CommonModule } from '@angular/common'
import { Component } from '@angular/core'
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms'
import { Router, RouterLink } from '@angular/router'
import { firstValueFrom } from 'rxjs'

import { ButtonComponent } from '../../components/button/button.component'
import { LogoComponent } from '../../components/logo/logo.component'
import { AuthService } from '../../services/auth.service'
import { extractErrorMessage } from '../../util/extract-error-message'

interface FormValues {
  username: string
  password: string
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ButtonComponent, LogoComponent, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  public loginForm = this.fb.group<Record<keyof FormValues, FormControl<string>>>({
    username: this.fb.control('', { nonNullable: true, validators: [Validators.required] }),
    password: this.fb.control('', { nonNullable: true, validators: [Validators.required] }),
  })
  private isSubmitted = false
  public isSubmitting = false

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  /**
   * Checks if the error for the given input key should be shown.
   * Respects the form state and the input state (dirty, touched).
   * @param key The input key to check.
   * @return True if the error should be shown, false otherwise.
   */
  shouldShowControlError(key: keyof FormValues): boolean {
    const control = this.loginForm.get(key)
    return !!control && control.invalid && (control.dirty || control.touched || this.isSubmitted)
  }

  /**
   * Handles the form submission.
   */
  async onSubmit() {
    this.isSubmitted = true
    const username = this.loginForm.value.username
    const password = this.loginForm.value.password
    if (!this.isSubmitting && this.loginForm.valid && username && password) {
      this.isSubmitting = true
      try {
        await firstValueFrom(this.authService.login(username, password))
        // Navigate to home page, once the registration was successful.
        await this.router.navigateByUrl('/')
      } catch (error) {
        const message = extractErrorMessage(error)
        if (message.includes('401')) {
          this.loginForm.setErrors({ invalidCredentials: true })
        } else {
          this.loginForm.setErrors({ unknownError: message })
        }
      } finally {
        this.isSubmitting = false
      }
    }
  }
}
