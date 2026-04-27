import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi   } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// BackOffice Components
import { DashboardComponent } from './dashboard/dashboard.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { HeaderComponent as BackHeaderComponent } from './layout/header/header.component';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { FooterComponent as BackFooterComponent } from './layout/footer/footer.component';
import { RouteMapComponent } from './dashboards/route-map/route-map.component';
import { SponsorComponent } from './dashboards/sponsor/sponsor.component';
import { GuideComponent } from './dashboards/guide/guide.component';
import { GearProviderComponent } from './dashboards/gear-provider/gear-provider.component';
import { BackofficeDeliveryComponent } from './dashboards/delivery/delivery.component';
import { AdminManagementComponent } from './dashboards/admin-management/admin-management.component';
import { UserManagementComponent } from './dashboards/user-management/user-management.component';

// FrontOffice Components
import { MainLayoutComponent as FrontMainLayoutComponent } from './frontoffice/layout/main-layout/main-layout.component';
import { HeaderComponent as FrontHeaderComponent } from './frontoffice/layout/header/header.component';
import { FooterComponent as FrontFooterComponent } from './frontoffice/layout/footer/footer.component';
import { HomeComponent } from './frontoffice/pages/home/home.component';
import { CampsitesComponent } from './frontoffice/pages/campsites/campsites.component';
import { OutdoorTripsComponent } from './frontoffice/pages/outdoor-trips/outdoor-trips.component';
import { ShopComponent } from './frontoffice/pages/shop/shop.component';
import { EventsComponent } from './frontoffice/pages/events/events.component';
import { ForumComponent } from './frontoffice/pages/forum/forum.component';
import { MessagesComponent } from './frontoffice/pages/messages/messages.component';
import { LoginComponent } from './frontoffice/pages/login/login.component';
import { RegisterComponent } from './frontoffice/pages/register/register.component';
import { ProfileComponent } from './frontoffice/pages/profile/profile.component';
import { DeliveryComponent } from './frontoffice/pages/delivery/delivery.component';
import { CampsiteOwnerComponent } from './dashboards/campsite-owner/campsite-owner.component';
import { CampsiteManagerComponent } from './dashboards/campsite-manager/campsite-manager.component';
import { ForumModeratorComponent } from './dashboards/forum-moderator/forum-moderator.component';
import { EventOrganizerComponent } from './dashboards/event-organizer/event-organizer.component';
import { CartComponent } from './frontoffice/pages/cart/cart.component';
import { BookingsComponent } from './frontoffice/pages/bookings/bookings.component';
import { ProductDetailComponent } from './frontoffice/pages/product-detail/product-detail.component';
import { PaymentComponent } from './frontoffice/pages/payment/payment.component';
import { OrderTrackingComponent } from './frontoffice/pages/order-tracking/order-tracking.component';
import { ForgotPasswordComponent } from './frontoffice/pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './frontoffice/pages/reset-password/reset-password.component';
import { GuidesComponent } from './frontoffice/pages/guides/guides.component';
import { AssignmentsComponent } from './frontoffice/pages/assignments/assignments.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';

// Module: Official Campsite & Booking
import { CampsiteDetailComponent } from './frontoffice/pages/campsite-detail/campsite-detail.component';
import { CampsiteBookingComponent } from './frontoffice/pages/campsite-booking/campsite-booking.component';
import { MyBookingsComponent } from './frontoffice/pages/my-bookings/my-bookings.component';

// Module: Outdoor Campsite & Booking
import { ProposeOutdoorComponent } from './frontoffice/pages/propose-outdoor/propose-outdoor.component';
import { MyProposalsComponent } from './frontoffice/pages/my-proposals/my-proposals.component';
import { AdminOutdoorModerationComponent } from './dashboards/admin-outdoor-moderation/admin-outdoor-moderation.component';
import { OutdoorCampsiteDetailComponent } from './frontoffice/pages/outdoor-campsite-detail/outdoor-campsite-detail.component';
import { OutdoorBookingComponent } from './frontoffice/pages/outdoor-booking/outdoor-booking.component';
import { CampsitePaymentComponent } from './frontoffice/pages/campsite-payment/campsite-payment.component';
import { CampsiteAnalyticsComponent } from './dashboards/campsite-analytics/campsite-analytics.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    MainLayoutComponent,
    BackHeaderComponent,
    SidebarComponent,
    BackFooterComponent,
    RouteMapComponent,
    SponsorComponent,
    GuideComponent,
    GearProviderComponent,
    FrontMainLayoutComponent,
    FrontHeaderComponent,
    FrontFooterComponent,
    HomeComponent,
    CampsitesComponent,
    OutdoorTripsComponent,
    ShopComponent,
    EventsComponent,
    ForumComponent,
    MessagesComponent,
    LoginComponent,
    RegisterComponent,
    DeliveryComponent,
    CampsiteOwnerComponent,
    CampsiteManagerComponent,
    ForumModeratorComponent,
    EventOrganizerComponent,
    CartComponent,
    BookingsComponent,
    ProductDetailComponent,
    ProfileComponent,
    BackofficeDeliveryComponent,
    AdminManagementComponent,
    UserManagementComponent,
    PaymentComponent,
    OrderTrackingComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    GuidesComponent,
    AssignmentsComponent,
    // Module: Official Campsite & Booking
    CampsiteDetailComponent,
    CampsiteBookingComponent,
    MyBookingsComponent,
    // Module: Outdoor Campsite & Booking
    ProposeOutdoorComponent,
    MyProposalsComponent,
    AdminOutdoorModerationComponent,
    OutdoorCampsiteDetailComponent,
    OutdoorBookingComponent,
    CampsitePaymentComponent,
    CampsiteAnalyticsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    HttpClientModule,
    RouterModule
  ],
  providers: [
    provideHttpClient(withFetch(), withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
