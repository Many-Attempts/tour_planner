import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Injectable()
export class LoginViewModel {
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly isLogin = signal(true);
  readonly name = signal('');
  readonly email = signal('');
  readonly password = signal('');
  readonly confirmPassword = signal('');
  readonly error = signal('');

  init(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  toggleMode(): void {
    this.isLogin.update(v => !v);
    this.error.set('');
  }

  submit(): void {
    this.error.set('');

    if (!this.isLogin() && this.password() !== this.confirmPassword()) {
      this.error.set('Passwords do not match');
      return;
    }

    if (this.isLogin()) {
      this.authService.login({ email: this.email(), password: this.password() }).subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => this.error.set(this.parseError(err, 'Login failed'))
      });
    } else {
      this.authService.register({ username: this.name(), email: this.email(), password: this.password() }).subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => this.error.set(this.parseError(err, 'Registration failed'))
      });
    }
  }

  private parseError(err: any, fallback: string): string {
    if (err.error?.message) {
      return err.error.message;
    }
    if (err.error?.errors) {
      return Object.values(err.error.errors).join(', ');
    }
    return fallback;
  }
}
