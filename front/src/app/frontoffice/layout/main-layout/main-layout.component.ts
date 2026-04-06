import { Component, OnInit, OnDestroy } from '@angular/core';
import { DeliveryService } from '../../../services/delivery.service';
import { AuthService } from '../../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  private userSub: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private deliveryService: DeliveryService
  ) {}

  ngOnInit(): void {
    this.userSub = this.authService.currentUser$.subscribe((user: any) => {
      // ✅ Polling uniquement pour les agents de livraison
      if (user && (user.role as string) === 'DELIVERYAGENT') {
        this.deliveryService.startPolling(user.id, user.role);
      } else {
        // ✅ Arrêter le polling si rôle différent ou déconnecté
        this.deliveryService.stopPolling();
      }
    });
  }

  ngOnDestroy(): void {
    this.deliveryService.stopPolling();
    this.userSub?.unsubscribe();
  }
}