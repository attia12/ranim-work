import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../../models/user.model';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit {
  pageTitle: string = 'Backoffice Management';

  constructor(private authService: AuthService) { }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.pageTitle = this.getRoleTitle(user.role);
      }
    });
  }

  getRoleTitle(role: UserRole): string {
    switch (role) {
      case UserRole.ADMIN: return 'System Administrator';
      case UserRole.GEAR_PROVIDER: return 'Gear Provider Dashboard';
      case UserRole.CAMPSITE_OWNER: return 'Campsite Management';
      case UserRole.CAMPSITE_MANAGER: return 'Campsite Manager';
      case UserRole.SPONSOR: return 'Sponsorship Hub';
      case UserRole.DELIVERY_PERSONNEL: return 'Delivery Assignments';
      case UserRole.PARTNER: return 'Partner Management';
      case UserRole.GUIDE: return 'Tour Guide Panel';
      case UserRole.COACH: return 'Coach Dashboard';
      default: return 'Backoffice Management';
    }
  }
}
