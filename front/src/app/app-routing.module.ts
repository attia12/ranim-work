import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

// BackOffice Imports
import { MainLayoutComponent as BackofficeLayout } from './layout/main-layout/main-layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { RouteMapComponent } from './dashboards/route-map/route-map.component';
import { SponsorComponent } from './dashboards/sponsor/sponsor.component';
import { GuideComponent } from './dashboards/guide/guide.component';
import { GearProviderComponent } from './dashboards/gear-provider/gear-provider.component';
import { CampsiteOwnerComponent } from './dashboards/campsite-owner/campsite-owner.component';
import { CampsiteManagerComponent } from './dashboards/campsite-manager/campsite-manager.component';
import { BackofficeDeliveryComponent } from './dashboards/delivery/delivery.component';
import { ForumModeratorComponent } from './dashboards/forum-moderator/forum-moderator.component';
import { EventOrganizerComponent } from './dashboards/event-organizer/event-organizer.component';
import { AdminManagementComponent } from './dashboards/admin-management/admin-management.component';
import { UserManagementComponent } from './dashboards/user-management/user-management.component';

// FrontOffice Imports
import { MainLayoutComponent as FrontofficeLayout } from './frontoffice/layout/main-layout/main-layout.component';
import { HomeComponent } from './frontoffice/pages/home/home.component';
import { CampsitesComponent } from './frontoffice/pages/campsites/campsites.component';
import { ShopComponent } from './frontoffice/pages/shop/shop.component';
import { EventsComponent } from './frontoffice/pages/events/events.component';
import { GuidesComponent }  from './frontoffice/pages/guides/guides.component';
import { AssignmentsComponent } from './frontoffice/pages/assignments/assignments.component'
import { ForumComponent } from './frontoffice/pages/forum/forum.component';
import { MessagesComponent } from './frontoffice/pages/messages/messages.component';
import { LoginComponent } from './frontoffice/pages/login/login.component';
import { RegisterComponent } from './frontoffice/pages/register/register.component';
import { DeliveryComponent } from './frontoffice/pages/delivery/delivery.component';
import { CartComponent } from './frontoffice/pages/cart/cart.component';
import { BookingsComponent } from './frontoffice/pages/bookings/bookings.component';
import { ProductDetailComponent } from './frontoffice/pages/product-detail/product-detail.component';
import { ProfileComponent } from './frontoffice/pages/profile/profile.component';
import { PaymentComponent } from './frontoffice/pages/payment/payment.component';
import { OrderTrackingComponent } from './frontoffice/pages/order-tracking/order-tracking.component';
import { ForgotPasswordComponent } from './frontoffice/pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './frontoffice/pages/reset-password/reset-password.component';

// Module: Official Campsite & Booking
import { CampsiteDetailComponent } from './frontoffice/pages/campsite-detail/campsite-detail.component';
import { CampsiteBookingComponent } from './frontoffice/pages/campsite-booking/campsite-booking.component';
import { MyBookingsComponent } from './frontoffice/pages/my-bookings/my-bookings.component';

// Module: Outdoor Campsite & Booking
import { OutdoorTripsComponent } from './frontoffice/pages/outdoor-trips/outdoor-trips.component';
import { ProposeOutdoorComponent } from './frontoffice/pages/propose-outdoor/propose-outdoor.component';
import { MyProposalsComponent } from './frontoffice/pages/my-proposals/my-proposals.component';
import { AdminOutdoorModerationComponent } from './dashboards/admin-outdoor-moderation/admin-outdoor-moderation.component';
import { OutdoorCampsiteDetailComponent } from './frontoffice/pages/outdoor-campsite-detail/outdoor-campsite-detail.component';
import { OutdoorBookingComponent } from './frontoffice/pages/outdoor-booking/outdoor-booking.component';

import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';
import { requireAuthGuard } from './guards/require-auth.guard';

const routes: Routes = [
  // Redirect root to front office
  { path: '', redirectTo: '/home', pathMatch: 'full' },

  // Standalone auth pages (no header/footer)
  { path: 'login', component: LoginComponent, canActivate: [authGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [authGuard] },

  // Front Office Routes
  {
    path: '',
    component: FrontofficeLayout,
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'campsites', component: CampsitesComponent },
      { path: 'shop', component: ShopComponent },
      { path: 'shop/product/:id', component: ProductDetailComponent },
      { path: 'cart', component: CartComponent },
      { path: 'bookings', component: BookingsComponent },
      { path: 'events', component: EventsComponent },
      { path: 'forum', component: ForumComponent },
      { path: 'messages', component: MessagesComponent },
      { path: 'forgot-password', component: ForgotPasswordComponent },
      { path: 'reset-password', component: ResetPasswordComponent },
      { path: 'delivery', component: DeliveryComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'payment/:orderId', component: PaymentComponent },
      { path: 'orders/:orderId', component: OrderTrackingComponent },
      { path: 'guides',      component: GuidesComponent },
      { path: 'assignments', component: AssignmentsComponent },

      // Module: Official Campsite & Booking
      { path: 'campsites/:id',       component: CampsiteDetailComponent },
      { path: 'campsites/:id/book',  component: CampsiteBookingComponent, canActivate: [requireAuthGuard] },
      { path: 'my-bookings',         component: MyBookingsComponent,      canActivate: [requireAuthGuard] },

      // Module: Outdoor Campsite & Booking
      { path: 'outdoor-campsites',          component: OutdoorTripsComponent },
      { path: 'outdoor-campsites/:id',      component: OutdoorCampsiteDetailComponent },
      { path: 'outdoor-campsites/:id/book', component: OutdoorBookingComponent, canActivate: [requireAuthGuard] },
      { path: 'propose-outdoor',            component: ProposeOutdoorComponent,  canActivate: [requireAuthGuard] },
      { path: 'my-proposals',               component: MyProposalsComponent,     canActivate: [requireAuthGuard] }
    ]
  },

  // Back Office Routes
  {
    path: 'admin',
    component: BackofficeLayout,
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users', component: UserManagementComponent },
      { path: 'route-map', component: RouteMapComponent },
      { path: 'sponsor', component: SponsorComponent },
      { path: 'guide', component: GuideComponent },
      { path: 'gear-provider', component: GearProviderComponent },
      { path: 'campsites', component: CampsiteOwnerComponent },
      { path: 'campsite-manager', component: CampsiteManagerComponent },
      { path: 'deliveries', component: BackofficeDeliveryComponent },
      { path: 'forum-mod', component: ForumModeratorComponent },
      { path: 'events', component: EventOrganizerComponent },
      { path: 'categories', component: AdminManagementComponent, data: { type: 'categories' } },
      { path: 'forum-categories', component: AdminManagementComponent, data: { type: 'forum-categories' } },
      { path: 'gear-reviews', component: AdminManagementComponent, data: { type: 'gear-reviews' } },
      { path: 'payments', component: AdminManagementComponent, data: { type: 'payments' } },
      { path: 'delivery-reviews', component: AdminManagementComponent, data: { type: 'delivery-reviews' } },
      { path: 'orders', component: AdminManagementComponent, data: { type: 'orders' } },
      { path: 'licenses', component: AdminManagementComponent, data: { type: 'licenses' } },
      { path: 'contracts', component: AdminManagementComponent, data: { type: 'contracts' } },

      // Module: Outdoor Campsite & Booking (Admin)
      { path: 'outdoor-moderation', component: AdminOutdoorModerationComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes), CommonModule],
  exports: [RouterModule]
})
export class AppRoutingModule { }
