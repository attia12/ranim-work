import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../../models/user.model';
import { Router } from '@angular/router';

interface MenuItem {
  title: string;
  icon: string;
  link?: string;
  roles: UserRole[];
  queryParams?: any;
  children?: MenuItem[];
  expanded?: boolean;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  menuItems: MenuItem[] = [
    { title: 'Dashboard', icon: 'fa-chart-line', link: '/admin/dashboard', roles: [UserRole.ADMIN, UserRole.GEAR_PROVIDER, UserRole.CAMPSITE_OWNER, UserRole.CAMPSITE_MANAGER, UserRole.SPONSOR, UserRole.DELIVERY_PERSONNEL, UserRole.FORUM_MODERATOR, UserRole.GUIDE, UserRole.EVENT_ORGANIZER] },
    {
      title: 'Management', icon: 'fa-cogs', roles: [UserRole.ADMIN], expanded: false,
      children: [
        { title: 'Users', icon: 'fa-users', link: '/admin/users', roles: [UserRole.ADMIN] },
        { title: 'Equipment', icon: 'fa-tools', link: '/admin/gear-provider', roles: [UserRole.ADMIN] },
        { title: 'Categories', icon: 'fa-list', link: '/admin/categories', roles: [UserRole.ADMIN] },
        { title: 'Forum Categories', icon: 'fa-comments', link: '/admin/forum-categories', roles: [UserRole.ADMIN] },
        { title: 'Gear Reviews', icon: 'fa-star', link: '/admin/gear-reviews', roles: [UserRole.ADMIN] },
        { title: 'Payments', icon: 'fa-credit-card', link: '/admin/payments', roles: [UserRole.ADMIN] },
        { title: 'Orders', icon: 'fa-box', link: '/admin/orders', roles: [UserRole.ADMIN] },
        { title: 'System Deliveries', icon: 'fa-truck', link: '/admin/deliveries', roles: [UserRole.ADMIN] },
        { title: 'License Management', icon: 'fa-id-card', link: '/admin/licenses', roles: [UserRole.ADMIN] },
        { title: 'Delivery Reviews', icon: 'fa-comment-alt', link: '/admin/delivery-reviews', roles: [UserRole.ADMIN] },
        { title: 'Campsites', icon: 'fa-campground', link: '/admin/campsites', roles: [UserRole.ADMIN] },
        { title: 'Wild Campsites', icon: 'fa-map-marked-alt', link: '/admin/route-map', roles: [UserRole.ADMIN] },
        { title: 'Sponsorships', icon: 'fa-handshake', link: '/admin/sponsor', roles: [UserRole.ADMIN] },
        { title: 'Contracts', icon: 'fa-file-signature', link: '/admin/contracts', roles: [UserRole.ADMIN] },
        { title: 'Forum Moderation', icon: 'fa-comments', link: '/admin/forum-mod', roles: [UserRole.ADMIN] },
        { title: 'Trips', icon: 'fa-compass', link: '/admin/guide', roles: [UserRole.ADMIN] },
        { title: 'Events', icon: 'fa-calendar-alt', link: '/admin/events', roles: [UserRole.ADMIN] },
        { title: 'Outdoor Moderation', icon: 'fa-mountain', link: '/admin/outdoor-moderation', roles: [UserRole.ADMIN] },
        { title: 'Booking Management', icon: 'fa-calendar-check', link: '/admin/campsite-manager', roles: [UserRole.ADMIN] }
      ]
    },
    { title: 'My Gear', icon: 'fa-shopping-bag', link: '/admin/gear-provider', roles: [UserRole.GEAR_PROVIDER] },
    { title: 'Campsites', icon: 'fa-campground', link: '/admin/campsites', roles: [UserRole.CAMPSITE_OWNER] },
    { title: 'My Campsites', icon: 'fa-campground', link: '/admin/campsite-manager', roles: [UserRole.CAMPSITE_MANAGER] },
    { title: 'Sponsorships', icon: 'fa-handshake', link: '/admin/sponsor', roles: [UserRole.SPONSOR] },
    { title: 'Available Deliveries', icon: 'fa-truck-loading', link: '/admin/deliveries', roles: [UserRole.DELIVERY_PERSONNEL], queryParams: { tab: 'available' } },
    { title: 'My Deliveries', icon: 'fa-shipping-fast', link: '/admin/deliveries', roles: [UserRole.DELIVERY_PERSONNEL], queryParams: { tab: 'managed' } },
    { title: 'My Vehicles', icon: 'fa-car', link: '/admin/deliveries', roles: [UserRole.DELIVERY_PERSONNEL], queryParams: { tab: 'vehicles' } },
    { title: 'Forum Moderation', icon: 'fa-comments', link: '/admin/forum-mod', roles: [UserRole.FORUM_MODERATOR] },
    { title: 'Trips', icon: 'fa-compass', link: '/admin/guide', roles: [UserRole.GUIDE] },
    { title: 'Events', icon: 'fa-calendar-alt', link: '/admin/events', roles: [UserRole.EVENT_ORGANIZER] },
    { title: 'Profile', icon: 'fa-user', link: '/profile', roles: [UserRole.ADMIN, UserRole.GEAR_PROVIDER, UserRole.CAMPSITE_OWNER, UserRole.CAMPSITE_MANAGER, UserRole.SPONSOR, UserRole.DELIVERY_PERSONNEL, UserRole.FORUM_MODERATOR, UserRole.GUIDE, UserRole.EVENT_ORGANIZER] },
    { title: 'Back to Site', icon: 'fa-home', link: '/home', roles: [UserRole.ADMIN, UserRole.GEAR_PROVIDER, UserRole.CAMPSITE_OWNER, UserRole.CAMPSITE_MANAGER, UserRole.SPONSOR, UserRole.DELIVERY_PERSONNEL, UserRole.FORUM_MODERATOR, UserRole.GUIDE, UserRole.EVENT_ORGANIZER] },
    { title: 'Logout', icon: 'fa-sign-out-alt', link: '/logout', roles: [UserRole.ADMIN, UserRole.GEAR_PROVIDER, UserRole.CAMPSITE_OWNER, UserRole.CAMPSITE_MANAGER, UserRole.SPONSOR, UserRole.DELIVERY_PERSONNEL, UserRole.FORUM_MODERATOR, UserRole.GUIDE, UserRole.EVENT_ORGANIZER] },
  ];

  filteredMenuItems: MenuItem[] = [];
  panelLabel: string = 'Panel';

  toggleMenu(item: MenuItem, event: Event): void {
    if (item.children) {
      event.preventDefault();
      item.expanded = !item.expanded;
    }
  }

  constructor(
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        // Update the main Dashboard link based on role
        const dashItem = this.menuItems.find(item => item.title === 'Dashboard');
        if (dashItem) {
          dashItem.link = this.getDashboardLink(user.role);
        }
        this.filteredMenuItems = this.menuItems.filter(item => item.roles.includes(user.role));
        this.panelLabel = this.getPanelLabel(user.role);
      }
    });
  }

  getPanelLabel(role: UserRole): string {
    switch (role) {
      case UserRole.ADMIN:              return 'Admin Panel';
      case UserRole.GEAR_PROVIDER:      return 'Equipment Provider';
      case UserRole.CAMPSITE_OWNER:     return 'Campsite Owner';
      case UserRole.CAMPSITE_MANAGER:   return 'Campsite Manager';
      case UserRole.SPONSOR:            return 'Sponsor Panel';
      case UserRole.DELIVERY_PERSONNEL: return 'Delivery Agent';
      case UserRole.FORUM_MODERATOR:    return 'Forum Moderator';
      case UserRole.GUIDE:              return 'Guide Panel';
      case UserRole.EVENT_ORGANIZER:    return 'Event Organizer';
      default:                          return 'Panel';
    }
  }

  getDashboardLink(role: UserRole): string {
    switch (role) {
      case UserRole.ADMIN: return '/admin/dashboard';
      case UserRole.GEAR_PROVIDER: return '/admin/gear-provider';
      case UserRole.CAMPSITE_OWNER: return '/admin/campsites';
      case UserRole.CAMPSITE_MANAGER: return '/admin/campsite-manager';
      case UserRole.SPONSOR: return '/admin/sponsor';
      case UserRole.DELIVERY_PERSONNEL: return '/admin/deliveries';
      case UserRole.FORUM_MODERATOR: return '/admin/forum-mod';
      case UserRole.GUIDE: return '/admin/guide';
      case UserRole.EVENT_ORGANIZER: return '/admin/events';
      default: return '/admin/dashboard';
    }
  }

  onLogout(event: Event): void {
    event.preventDefault();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
