import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  isLogin = true;
  name = '';
  email = '';
  password = '';
  confirmPassword = '';
  error = '';

  constructor(private authService: AuthService, private router: Router) {
    if (authService.isAuthenticated()) {
      router.navigate(['/dashboard']);
    }
  }

  toggleMode(): void {
    this.isLogin = !this.isLogin;
    this.error = '';
  }

  onSubmit(): void {
    this.error = '';

    if (!this.isLogin && this.password !== this.confirmPassword) {
      this.error = 'Passwords do not match';
      return;
    }

    if (this.isLogin) {
      this.authService.login({ email: this.email, password: this.password }).subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => this.error = err.error?.message || 'Login failed'
      });
    } else {
      this.authService.register({ username: this.name, email: this.email, password: this.password }).subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => this.error = err.error?.message || 'Registration failed'
      });
    }
  }
}
