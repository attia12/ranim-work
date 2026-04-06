import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  email = '';
  submitted = false;
  loading = false;
  errorMsg = '';

  constructor(private authService: AuthService) {}

  onSubmit() {
    this.loading = true;
    this.errorMsg = '';
    
    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.submitted = true;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err.error?.message || err.error?.error || 'An error occurred. Please try again.';
      }
    });
  }
}
