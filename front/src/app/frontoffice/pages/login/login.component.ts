import { Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  credentials = {
    email: '',
    password: ''
  };
  showPassword = false;
  rememberMe = false;
  isLoading = false;
  loginError = '';

  constructor(private authService: AuthService, private router: Router) { }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit(form: NgForm) {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.loginError = '';

    this.authService.login(this.credentials.email, this.credentials.password).subscribe({
      next: (user) => {
        this.isLoading = false;
        if (user) {
          this.router.navigate(['/home']);
        } else {
          this.loginError = 'Invalid email or password. Please try again.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.loginError = err.error?.message || 'Login failed. Please check your credentials.';
      }
    });
  }
}
