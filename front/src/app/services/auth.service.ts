import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable, tap, mergeMap } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';
import { CartService } from './cart.service';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object,
    private cartService: CartService,
    private notifService: NotificationService
  ) {
    if (isPlatformBrowser(this.platformId)) {
      const savedUser = localStorage.getItem('currentUser');
      if (savedUser) {
        const user = JSON.parse(savedUser);
        this.currentUserSubject.next(user);
        // Restore notifications for the saved user on page refresh
        this.notifService.initForUser(user.id?.toString() || '');
      }
    }
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password })
      .pipe(
        tap(response => {
          if (response && response.token) {
            // Save token first so the subsequent getMe() request is authenticated
            if (isPlatformBrowser(this.platformId)) {
              localStorage.setItem('token', response.token);
              localStorage.setItem('userId', response.userId?.toString() || '');
            }
            // Store a minimal user immediately so the app isn't blocked
            const partialUser: User = {
              id: response.userId?.toString() || '',
              firstName: response.fullname?.split(' ')[0] || '',
              lastName: response.fullname?.split(' ').slice(1).join(' ') || '',
              email: email,
              phoneNumber: response.phoneNumber || '',
              role: response.role as any
            };
            this.setCurrentUser(partialUser);
          }
        }),
        // After the token is stored, fetch the full profile so all fields are populated
        mergeMap(response => {
          if (response && response.token) {
            return this.getMe();
          }
          return [response];
        })
      );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData, { responseType: 'text' });
  }

  updateProfile(userId: string, userData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/profile/${userId}`, userData)
      .pipe(tap(response => {
        if (response && response.token) {
          const user: User = {
            id: response.userId?.toString() || '',
            firstName: response.firstName || '',
            lastName: response.lastName || '',
            email: this.getCurrentUser()?.email || '',
            phoneNumber: response.phoneNumber || '',
            role: response.role as any,
            avatar: response.avatar || '',
            address: response.address || '',
            city: response.city || '',
            companyName: response.companyName || '',
            shopDescription: response.shopDescription || '',
            licenseNumber: response.licenseNumber || '',
            isVerified: response.isVerified || false
          };
          this.setCurrentUser(user);
        }
      }));
  }

  getMe(): Observable<User> {
    return this.http.get<any>(`${this.apiUrl}/me`)
      .pipe(tap(data => {
        const user: User = {
          id: data.id?.toString() || '',
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          email: data.email || '',
          phoneNumber: data.phoneNumber || '',
          role: data.role as any,
          avatar: data.avatar || '',
          address: data.address || '',
          city: data.city || '',
          // provider
          companyName: data.companyName || '',
          shopDescription: data.shopDescription || '',
          // agent
          licenseNumber: data.licenseNumber || '',
          isVerified: data.isVerified || false,
          // camper
          experienceLevel: data.experienceLevel || '',
          preferredTerrain: data.preferredTerrain || '',
          bio: data.bio || '',
          totalTrips: data.totalTrips || 0,
          // campsite owner
          businessName: data.businessName || '',
          businessDescription: data.businessDescription || '',
          websiteUrl: data.websiteUrl || '',
          region: data.region || '',
          // guide
          specialization: data.specialization || '',
          yearsExperience: data.yearsExperience || 0,
          languages: data.languages || '',
          certificationNumber: data.certificationNumber || '',
          // coach
          coachingType: data.coachingType || '',
          certifications: data.certifications || '',
          // sponsor
          sponsorshipDescription: data.sponsorshipDescription || '',
          logoUrl: data.logoUrl || '',
          industry: data.industry || '',
          // partner
          organizationName: data.organizationName || '',
          partnershipDescription: data.partnershipDescription || '',
          partnerType: data.partnerType || '',
        };
        this.setCurrentUser(user);
        return user;
      }));
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, { token, newPassword });
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('currentUser');
      localStorage.removeItem('token');
    }
    this.cartService.clearCartOnLogout();
    // Clear notification session — each user sees only their own notifications
    this.notifService.clearSession();
    this.currentUserSubject.next(null);
  }

  private setCurrentUser(user: User) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('currentUser', JSON.stringify(user));
    }
    this.currentUserSubject.next(user);
    // Load this user's personal notifications
    this.notifService.initForUser(user.id?.toString() || '');
  }

  isLoggedIn(): boolean {
    return !!this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    return this.currentUserSubject.value?.role === role;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token');
    }
    return null;
  }
}
