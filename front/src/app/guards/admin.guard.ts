import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user.model';

export const adminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.getCurrentUser();

    if (user && user.role !== UserRole.CAMPER) {
        return true;
    }

    // Redirect to home if not authorized
    router.navigate(['/home']);
    return false;
};
