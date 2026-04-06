import { Component, OnInit, AfterViewInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { UserRole } from '../../../models/user.model';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit, AfterViewInit {
  get UserRole() { return UserRole; }

  userData = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    role: '' as any,
    // CAMPER
    experienceLevel: '',
    preferredTerrain: '',
    bio: '',
    // GEAR_PROVIDER & SPONSOR share companyName
    companyName: '',
    shopDescription: '',
    // CAMPSITE_OWNER
    businessName: '',
    businessDescription: '',
    websiteUrl: '',
    region: '',
    // GUIDE
    specialization: '',
    yearsExperience: null as number | null,
    languages: '',
    certificationNumber: '',
    // SPONSOR
    sponsorshipDescription: '',
    logoUrl: '',
    industry: '',
    // DELIVERY
    licenseNumber: '',
    // PARTNER
    organizationName: '',
    partnershipDescription: '',
    partnerType: '',
    managedRegion: '',
    managerNotes: '',
    // EVENT_ORGANIZER
    eventBio: '',
    eventSpecialization: '',
    eventYearsExperience: null as number | null
  };

  showPassword        = false;
  showConfirmPassword = false;
  isLoading           = false;
  registerError       = '';
  acceptedTerms       = false;
  passwordMismatch    = false;
  roleRequired        = false;

  // Password strength: 0–4
  passwordStrength      = 0;
  passwordStrengthLabel = '';

  roles = [
    { value: UserRole.CAMPER,             label: 'Camper',          icon: 'fa-hiking',         description: 'Explore campsites and rent gear' },
    { value: UserRole.GEAR_PROVIDER,      label: 'Gear Provider',   icon: 'fa-tools',          description: 'List and rent out camping equipment' },
    { value: UserRole.CAMPSITE_OWNER,     label: 'Campsite Owner',  icon: 'fa-campground',     description: 'List and manage your campsites' },
    { value: UserRole.GUIDE,              label: 'Guide',           icon: 'fa-compass',        description: 'Lead outdoor trips and expeditions' },
    { value: UserRole.SPONSOR,            label: 'Sponsor',         icon: 'fa-handshake',      description: 'Sponsor events and community activities' },
    { value: UserRole.DELIVERY_PERSONNEL, label: 'Delivery Agent',  icon: 'fa-truck',          description: 'Deliver equipment to customers' },
    { value: UserRole.CAMPSITE_MANAGER,   label: 'Site Manager',    icon: 'fa-clipboard-list', description: 'Manage operations of a campsite' },
    { value: UserRole.PARTNER,            label: 'Partner',         icon: 'fa-users',          description: 'Collaborate as an organization or community' },
    { value: UserRole.FORUM_MODERATOR,    label: 'Moderator',       icon: 'fa-shield-alt',     description: 'Moderate the community forum' },
    { value: UserRole.EVENT_ORGANIZER,    label: 'Event Organizer', icon: 'fa-calendar-alt',   description: 'Plan and manage outdoor events' },
  ];

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    // Nothing needed on init for the register page
  }

  ngAfterViewInit(): void {
    if (typeof window !== 'undefined' && (window as any).bootstrap) {
      document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
        new (window as any).bootstrap.Tooltip(el);
      });
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  checkPasswordMatch(): void {
    this.passwordMismatch =
      !!this.userData.confirmPassword &&
      this.userData.password !== this.userData.confirmPassword;
  }

  updatePasswordStrength(): void {
    const pwd = this.userData.password;
    let score = 0;
    if (pwd.length >= 8)            score++;
    if (/[A-Z]/.test(pwd))          score++;
    if (/[0-9]/.test(pwd))          score++;
    if (/[^A-Za-z0-9]/.test(pwd))   score++;
    this.passwordStrength      = score;
    this.passwordStrengthLabel = ['', 'Weak', 'Fair', 'Good', 'Strong'][score] || '';
    this.checkPasswordMatch();
  }

  onSubmit(form: NgForm): void {
    form.control.markAllAsTouched();

    this.roleRequired = !this.userData.role;
    this.checkPasswordMatch();

    if (form.invalid || this.passwordMismatch || this.roleRequired) {
      return;
    }

    this.isLoading    = true;
    this.registerError = '';

    const { confirmPassword, eventBio, eventSpecialization, eventYearsExperience, ...rest } = this.userData;
    const payload: any = { ...rest };
    if (this.userData.role === UserRole.EVENT_ORGANIZER) {
      payload.bio             = eventBio;
      payload.specialization  = eventSpecialization;
      payload.yearsExperience = eventYearsExperience;
    }

    this.authService.register(payload).subscribe({
      next: () => {
        this.authService.login(this.userData.email, this.userData.password).subscribe({
          next: () => {
            this.isLoading = false;
            this.router.navigate(['/profile']);
          },
          error: () => {
            this.isLoading = false;
            this.router.navigate(['/login']);
          }
        });
      },
      error: (err) => {
        this.isLoading     = false;
        this.registerError = err.error?.error || 'Registration failed. Please check your details.';
      }
    });
  }
}
