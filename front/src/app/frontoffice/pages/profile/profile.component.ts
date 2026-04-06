import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../services/auth.service';
import { User, UserRole } from '../../../models/user.model';
import { environment } from '../../../../environments/environment';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
    user: User | null = null;
    editMode = false;
    editedUser: any = {};
    successMessage = '';
    errorMessage = '';
    isSaving = false;
    isSavingProfile = false;

    UserRole = UserRole;

    warehouses: any[] = [];

    // Role-specific profile objects
    providerProfile:      any = { companyName: '', shopDescription: '' };
    agentProfile:         any = { licenseNumber: '', isVerified: false };
    camperProfile:        any = { experienceLevel: '', preferredTerrain: '', bio: '', totalTrips: 0 };
    campsiteOwnerProfile: any = { businessName: '', businessDescription: '', websiteUrl: '', region: '', isVerified: false };
    guideProfile:         any = { specialization: '', bio: '', yearsExperience: 0, languages: '', certificationNumber: '', isVerified: false };
    coachProfile:         any = { coachingType: '', bio: '', yearsExperience: 0, certifications: '', isVerified: false };
    sponsorProfile:       any = { companyName: '', sponsorshipDescription: '', websiteUrl: '', logoUrl: '', industry: '' };
    partnerProfile:       any = { organizationName: '', partnershipDescription: '', websiteUrl: '', partnerType: '' };
    campsiteManagerProfile: any = { managedRegion: '', managerNotes: '', isVerified: false };
    eventOrganizerProfile: any = { organizationName: '', bio: '', specialization: '', websiteUrl: '', phoneContact: '', yearsExperience: 0, isVerified: false };

    constructor(
        private authService: AuthService,
        private http: HttpClient,
        @Inject(PLATFORM_ID) private platformId: Object
    ) {}

    ngOnInit(): void {
        this.authService.currentUser$.subscribe(user => {
            this.user = user;
            if (user) {
                this.editedUser = { ...user };
                this.loadRoleProfile(user);
            }
        });
    }

    loadRoleProfile(user: User): void {
        // Small delay to ensure JWT is in place from login/getMe
        setTimeout(() => this.doLoadRoleProfile(user), 100);
    }

    private doLoadRoleProfile(user: User): void {
        const base = environment.apiUrl;
        const role = user.role as string;

        if (role === 'EQUIPEMENTPROVIEDERS') {
            this.http.get<any>(`${base}/auth/profile/provider/${user.id}`)
                .subscribe({
                    next: p => { this.providerProfile = p; },
                    error: (err) => {
                        if (err.status !== 404) console.warn('Provider profile not found yet.');
                    }
                });
            this.loadWarehouses();
        }
        if (role === 'DELIVERYAGENT') {
            this.http.get<any>(`${base}/auth/profile/agent/${user.id}`)
                .subscribe({
                    next: p => { this.agentProfile = p; },
                    error: () => {}
                });
        }
        // For all other roles, getMe() already returns their profile fields — just copy from user object
        if (role === 'COMPERS') {
            this.camperProfile = {
                experienceLevel:  user.experienceLevel  || '',
                preferredTerrain: user.preferredTerrain || '',
                bio:              user.bio              || '',
                totalTrips:       user.totalTrips       || 0
            };
        }
        if (role === 'COMPSITEOWNERS') {
            this.campsiteOwnerProfile = {
                businessName:        user.businessName        || '',
                businessDescription: user.businessDescription || '',
                websiteUrl:          user.websiteUrl          || '',
                region:              user.region              || '',
                isVerified:          user.isVerified          || false
            };
        }
        if (role === 'GUIDE') {
            this.guideProfile = {
                specialization:      user.specialization      || '',
                bio:                 user.bio                 || '',
                yearsExperience:     user.yearsExperience     || 0,
                languages:           user.languages           || '',
                certificationNumber: user.certificationNumber || '',
                isVerified:          user.isVerified          || false
            };
        }
        if (role === 'COACH') {
            this.coachProfile = {
                coachingType:    user.coachingType    || '',
                bio:             user.bio             || '',
                yearsExperience: user.yearsExperience || 0,
                certifications:  user.certifications  || '',
                isVerified:      user.isVerified      || false
            };
        }
        if (role === 'SPONSORS') {
            this.sponsorProfile = {
                companyName:            user.companyName            || '',
                sponsorshipDescription: user.sponsorshipDescription || '',
                websiteUrl:             user.websiteUrl             || '',
                logoUrl:                user.logoUrl                || '',
                industry:               user.industry               || ''
            };
        }
        if (role === 'PARTENERS') {
            this.partnerProfile = {
                organizationName:       user.organizationName       || '',
                partnershipDescription: user.partnershipDescription || '',
                websiteUrl:             user.websiteUrl             || '',
                partnerType:            user.partnerType            || ''
            };
        }
        if (role === 'CAMPSITEMANAGER') {
            this.campsiteManagerProfile = {
                managedRegion: user.managedRegion || '',
                managerNotes:  user.managerNotes  || '',
                isVerified:    user.isVerified    || false
            };
        }
        if (role === 'EVENT_ORGANIZER') {
            this.eventOrganizerProfile = {
                organizationName: user.organizationName || '',
                bio:              user.bio              || '',
                specialization:   user.specialization   || '',
                websiteUrl:       user.websiteUrl       || '',
                phoneContact:     user.phoneContact     || '',
                yearsExperience:  user.yearsExperience  || 0,
                isVerified:       user.isVerified       || false
            };
        }
    }

    loadWarehouses(): void {
        this.http.get<any[]>(`${environment.apiUrl}/auth/profile/provider/warehouses`)
            .subscribe({ next: w => this.warehouses = w, error: () => {} });
    }

    toggleEdit(): void {
        this.editMode = !this.editMode;
        if (!this.editMode && this.user) this.editedUser = { ...this.user };
    }

    saveProfile(): void {
        if (!this.user?.id) return;
        this.isSaving = true;
        this.errorMessage = '';
        this.authService.updateProfile(this.user.id, this.editedUser).subscribe({
            next: () => {
                this.isSaving = false;
                this.editMode = false;
                this.successMessage = 'Profile updated!';
                setTimeout(() => this.successMessage = '', 3000);
            },
            error: () => {
                this.isSaving = false;
                this.errorMessage = 'Failed to update profile.';
            }
        });
    }

    saveRoleProfile(): void {
        if (!this.user) return;
        this.isSavingProfile = true;
        const base = environment.apiUrl;
        const role = this.user.role as string;

        const urlMap: Record<string, string> = {
            'EQUIPEMENTPROVIEDERS': `${base}/auth/profile/provider`,
            'DELIVERYAGENT':        `${base}/auth/profile/agent`,
            'COMPERS':              `${base}/auth/profile/camper`,
            'COMPSITEOWNERS':       `${base}/auth/profile/campsite-owner`,
            'GUIDE':                `${base}/auth/profile/guide`,
            'COACH':                `${base}/auth/profile/coach`,
            'SPONSORS':             `${base}/auth/profile/sponsor`,
            'PARTENERS':            `${base}/auth/profile/partner`,
            'CAMPSITEMANAGER':      `${base}/auth/profile/campsite-manager`,
            'EVENT_ORGANIZER':      `${base}/auth/profile/event-organizer`,
        };

        const bodyMap: Record<string, any> = {
            'EQUIPEMENTPROVIEDERS': this.providerProfile,
            'DELIVERYAGENT':        { licenseNumber: this.agentProfile.licenseNumber },
            'COMPERS':              this.camperProfile,
            'COMPSITEOWNERS':       this.campsiteOwnerProfile,
            'GUIDE':                { ...this.guideProfile, yearsExperience: String(this.guideProfile.yearsExperience) },
            'COACH':                { ...this.coachProfile, yearsExperience: String(this.coachProfile.yearsExperience) },
            'SPONSORS':             this.sponsorProfile,
            'PARTENERS':            this.partnerProfile,
            'CAMPSITEMANAGER':      {
                managedRegion: this.campsiteManagerProfile.managedRegion,
                managerNotes:  this.campsiteManagerProfile.managerNotes
            },
            'EVENT_ORGANIZER': {
                organizationName: this.eventOrganizerProfile.organizationName,
                bio:              this.eventOrganizerProfile.bio,
                specialization:   this.eventOrganizerProfile.specialization,
                websiteUrl:       this.eventOrganizerProfile.websiteUrl,
                phoneContact:     this.eventOrganizerProfile.phoneContact,
                yearsExperience:  this.eventOrganizerProfile.yearsExperience
            },
        };

        const url  = urlMap[role];
        const body = bodyMap[role];
        if (!url) { this.isSavingProfile = false; return; }

        this.http.put(url, body).subscribe({
            next: () => {
                this.isSavingProfile = false;
                this.successMessage = 'Profile saved!';
                setTimeout(() => this.successMessage = '', 3000);
                // Refresh full user data
                this.authService.getMe().subscribe();
            },
            error: () => {
                this.isSavingProfile = false;
                this.errorMessage = 'Failed to save profile.';
            }
        });
    }

    getRoleLabel(role: string): string {
        const labels: Record<string, string> = {
            COMPERS:             'Camper',
            EQUIPEMENTPROVIEDERS:'Equipment Provider',
            DELIVERYAGENT:       'Delivery Agent',
            ADMIN:               'Admin',
            COMPSITEOWNERS:      'Campsite Owner',
            CAMPSITEMANAGER:     'Campsite Manager',
            GUIDE:               'Guide',
            SPONSORS:            'Sponsor',
            PARTENERS:           'Partner',
            COACH:               'Coach',
            EVENT_ORGANIZER:     'Event Organizer'
        };
        return labels[role] || role;
    }

    getRoleBadgeColor(role: string): string {
        const colors: Record<string, string> = {
            COMPERS:             'badge-success',
            EQUIPEMENTPROVIEDERS:'badge-warning',
            DELIVERYAGENT:       'badge-primary',
            ADMIN:               'badge-danger',
            COMPSITEOWNERS:      'badge-info',
            CAMPSITEMANAGER:     'badge-success',
            GUIDE:               'badge-secondary',
            SPONSORS:            'badge-dark',
            PARTENERS:           'badge-secondary',
            COACH:               'badge-info',
            EVENT_ORGANIZER:     'badge-success'
        };
        return colors[role] || 'badge-secondary';
    }
}
