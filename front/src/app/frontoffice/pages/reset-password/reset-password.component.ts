import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  token: string | null = null;
  invalidToken = false;
  
  newPassword = '';
  confirmPassword = '';
  showPassword = false;
  
  loading = false;
  success = false;
  errorMsg = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token) {
      this.invalidToken = true;
    }
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.errorMsg = '';
    
    if (this.newPassword !== this.confirmPassword) {
      this.errorMsg = 'Passwords do not match';
      return;
    }

    if (!this.token) {
      this.errorMsg = 'Invalid session. Please request a new reset link.';
      return;
    }

    this.loading = true;
    
    this.authService.resetPassword(this.token, this.newPassword).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err.error?.message || err.error?.error || 'An error occurred. Please try again.';
      }
    });
  }
}
