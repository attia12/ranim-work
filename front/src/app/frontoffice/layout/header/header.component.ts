import { Component, OnInit } from '@angular/core';
import { NotificationService, AppNotification } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { CartService } from '../../../services/cart.service';
import { UserRole } from '../../../models/user.model';

@Component({
  selector: 'app-front-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  UserRole = UserRole;
  cartCount: number = 0;
  notifications: AppNotification[] = [];
  unreadCount = 0;
  showNotifPanel = false;

  constructor(
    public authService: AuthService,
    private router: Router,
    private cartService: CartService,
    private notifService: NotificationService
  ) {
    this.cartService.items$.subscribe(() => {
      this.cartCount = this.cartService.getCartCount();
    });
  }

  ngOnInit() {
    this.notifService.notifications$.subscribe(notifs => {
      this.notifications = notifs;
      this.unreadCount = notifs.filter(n => !n.read).length;
    });
  }

  toggleNotifPanel(e: Event) {
    e.stopPropagation();
    this.showNotifPanel = !this.showNotifPanel;
  }

  closeNotifPanel() {
    this.showNotifPanel = false;
  }

  openNotification(n: AppNotification): void {
      this.notifService.markRead(n.id);
      this.showNotifPanel = false;
      if (n.link) {
          this.router.navigateByUrl(n.link);
      }
  }

  markAllRead() {
    this.notifService.markAllRead();
  }

  clearAll() {
    this.notifService.clear();
  }

  timeAgo(date: Date): string {
    const now = new Date();
    const diff = Math.floor((now.getTime() - new Date(date).getTime()) / 1000);
    if (diff < 60) return 'just now';
    if (diff < 3600) return Math.floor(diff / 60) + 'm ago';
    if (diff < 86400) return Math.floor(diff / 3600) + 'h ago';
    return Math.floor(diff / 86400) + 'd ago';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  addToWishlist() {
    this.notifService.push({
      type: 'info',
      title: 'Wishlist Feature',
      message: 'Wishlist feature coming soon!',
      icon: 'fa-heart',
      iconColor: '#ef4444'
    });
  }
}
