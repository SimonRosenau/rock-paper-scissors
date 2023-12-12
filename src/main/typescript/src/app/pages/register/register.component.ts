import { CommonModule } from '@angular/common'
import { Component } from '@angular/core'
import { AbstractControl, FormBuilder, FormControl, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms'
import { Router, RouterLink } from '@angular/router'
import { firstValueFrom } from 'rxjs'

import { ButtonComponent } from '../../components/button/button.component'
import { LogoComponent } from '../../components/logo/logo.component'
import { AuthService } from '../../services/auth.service'
import { extractErrorMessage } from '../../util/extract-error-message'

const passwordPattern = new RegExp('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,}$')

interface FormValues {
  username: string
  password: string
  confirmPassword: string
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ButtonComponent, LogoComponent, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  public registerForm = this.fb.group<Record<keyof FormValues, FormControl<string>>>(
    {
      username: this.fb.control('', { nonNullable: true, validators: [Validators.required] }),
      password: this.fb.control('', { nonNullable: true, validators: [Validators.required, Validators.pattern(passwordPattern)] }),
      confirmPassword: this.fb.control('', { nonNullable: true, validators: [Validators.required, Validators.pattern(passwordPattern)] }),
    },
    { validators: this.checkPasswords }
  )
  private isSubmitted = false
  public isSubmitting = false

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  /**
   * Checks if the passwords match.
   * @param control The form control to check.
   * @return Null if the passwords match, otherwise the error object.
   */
  checkPasswords(control: AbstractControl<FormValues>): ValidationErrors | null {
    const password = control.get('password')?.value
    const confirmPassword = control.get('confirmPassword')?.value

    return password === confirmPassword ? null : { passwordMismatch: true }
  }

  /**
   * Checks if the error for the given input key should be shown.
   * Respects the form state and the input state (dirty, touched).
   * @param key The input key to check.
   * @return True if the error should be shown, false otherwise.
   */
  shouldShowControlError(key: keyof FormValues): boolean {
    const control = this.registerForm.get(key)
    if (control === null) return false
    const invalid = control.invalid || this.evaluateFormErrorKeys(key).some(errorKey => this.registerForm.hasError(errorKey))
    const shouldShow = control.dirty || control.touched || this.isSubmitted
    return invalid && shouldShow
  }

  /**
   * Evaluates the form error keys for the given input key.
   * Needed for special cases like password mismatch.
   * @param key The input key to evaluate.
   * @return The error keys for the given input key.
   */
  private evaluateFormErrorKeys(key: keyof FormValues): string[] {
    switch (key) {
      case 'username':
        return ['usernameTaken']
      case 'confirmPassword':
        return ['passwordMismatch']
      default:
        return []
    }
  }

  /**
   * Handles the form submission.
   */
  async onSubmit() {
    this.isSubmitted = true
    const username = this.registerForm.value.username
    const password = this.registerForm.value.password
    if (!this.isSubmitting && this.registerForm.valid && username && password) {
      this.isSubmitting = true
      try {
        await firstValueFrom(this.authService.register(username, password))
        // Navigate to home page, once the registration was successful.
        await this.router.navigateByUrl('/')
      } catch (error) {
        const message = extractErrorMessage(error)
        if (message.includes('already exists')) {
          this.registerForm.setErrors({ usernameTaken: true })
        } else {
          this.registerForm.setErrors({ unknownError: message })
        }
      } finally {
        this.isSubmitting = false
      }
    }
  }
}
