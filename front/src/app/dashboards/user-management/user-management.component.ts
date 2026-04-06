import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: any[] = []; // Changed to any[] to accommodate potential extra fields from API
  filteredUsers: any[] = [];

  // Stats
  totalUsers = 0;
  adminsCount = 0;

  // Filters
  searchTerm = '';
  roleFilter = '';

  // Modal State
  showModal = false;
  isEditing = false;
  currentUser: any = {
    role: 'COMPERS' // Default role
  };

  // Roles for dropdown
  roles = [
    'ADMIN',
    'COMPERS',
    'EQUIPEMENTPROVIEDERS',
    'COMPSITEOWNERS',
    'DELIVERYAGENT',
    'GUIDE',
    'SPONSORS',
    'PARTENERS'
  ];

  roleAttributes: Record<string, { label: string; fields: string[] }> = {
    DELIVERYAGENT: { label: 'Delivery Agent', fields: ['licenseNumber', 'isVerified'] },
    EQUIPEMENTPROVIEDERS: { label: 'Equipment Provider', fields: ['companyName', 'shopDescription'] },
    COMPSITEOWNERS: { label: 'Campsite Owner', fields: ['businessName', 'businessDescription', 'region', 'isVerified'] },
    GUIDE: { label: 'Guide', fields: ['specialization', 'certificationNumber', 'yearsExperience', 'isVerified'] },
    COACH: { label: 'Coach', fields: ['coachingType', 'certifications', 'yearsExperience', 'isVerified'] },
    SPONSORS: { label: 'Sponsor', fields: ['companyName', 'sponsorshipDescription', 'industry'] },
    PARTENERS: { label: 'Partner', fields: ['organizationName', 'partnershipDescription', 'partnerType'] },
    COMPERS: { label: 'Camper', fields: ['experienceLevel', 'preferredTerrain'] },
    ADMIN: { label: 'Admin', fields: [] }
  };

  roleProfileFields: Record<string, { key: string; label: string; type: 'text' | 'textarea' | 'number' | 'select'; placeholder?: string; options?: string[] }[]> = {
    EQUIPEMENTPROVIEDERS: [
        { key: 'companyName',     label: 'Company / Shop Name',  type: 'text',     placeholder: 'e.g. CampGear Tunisia' },
        { key: 'shopDescription', label: 'Shop Description',     type: 'textarea', placeholder: 'Describe your shop...' }
    ],
    DELIVERYAGENT: [
        { key: 'licenseNumber', label: "Driver's License Number", type: 'text', placeholder: 'e.g. TUN-2024-001234' }
    ],
    COMPSITEOWNERS: [
        { key: 'businessName',        label: 'Business Name',        type: 'text',     placeholder: 'e.g. Sahara Camps Tunisia' },
        { key: 'businessDescription', label: 'Business Description', type: 'textarea', placeholder: 'Describe your business...' },
        { key: 'websiteUrl',          label: 'Website',              type: 'text',     placeholder: 'https://...' },
        { key: 'region',              label: 'Region',               type: 'text',     placeholder: 'e.g. North Tunisia, Cap Bon' }
    ],
    GUIDE: [
        { key: 'specialization',      label: 'Specialization',       type: 'text',   placeholder: 'e.g. Desert Treks, Mountain Climbing' },
        { key: 'certificationNumber', label: 'Certification Number', type: 'text',   placeholder: 'Official certification ID' },
        { key: 'yearsExperience',     label: 'Years of Experience',  type: 'number', placeholder: '0' },
        { key: 'languages',           label: 'Languages (comma-separated)', type: 'text', placeholder: 'French, English, Arabic' },
        { key: 'bio',                 label: 'Bio',                  type: 'textarea', placeholder: 'Tell campers about yourself...' }
    ],
    COACH: [
        { key: 'coachingType',    label: 'Coaching Type',      type: 'text',     placeholder: 'e.g. Survival Skills, Rock Climbing' },
        { key: 'certifications',  label: 'Certifications',     type: 'text',     placeholder: 'e.g. BSAC Level 2, PADI Open Water' },
        { key: 'yearsExperience', label: 'Years of Experience', type: 'number',  placeholder: '0' },
        { key: 'bio',             label: 'Bio',                type: 'textarea', placeholder: 'Tell campers about yourself...' }
    ],
    SPONSORS: [
        { key: 'companyName',            label: 'Company Name',          type: 'text',     placeholder: 'e.g. OutdoorGear Co.' },
        { key: 'sponsorshipDescription', label: 'Sponsorship Description', type: 'textarea', placeholder: 'What do you offer as a sponsor?' },
        { key: 'websiteUrl',             label: 'Website',               type: 'text',     placeholder: 'https://...' },
        { key: 'industry',               label: 'Industry',              type: 'text',     placeholder: 'e.g. Outdoor Equipment, Sports Nutrition' }
    ],
    PARTENERS: [
        { key: 'organizationName',       label: 'Organization Name',     type: 'text',     placeholder: 'Your organization name' },
        { key: 'partnershipDescription', label: 'Partnership Description', type: 'textarea', placeholder: 'Describe the partnership offer...' },
        { key: 'websiteUrl',             label: 'Website',               type: 'text',     placeholder: 'https://...' },
        { key: 'partnerType',            label: 'Partner Type',          type: 'select',
          options: ['Event Organizer', 'Forum Moderator', 'Other'] }
    ],
    COMPERS: [
        { key: 'experienceLevel',  label: 'Experience Level', type: 'select',
          options: ['BEGINNER', 'INTERMEDIATE', 'EXPERT'] },
        { key: 'preferredTerrain', label: 'Preferred Terrain', type: 'select',
          options: ['MOUNTAINS', 'FOREST', 'DESERT', 'BEACH', 'MIXED'] }
    ],
    ADMIN: []
  };

  get currentRoleFields() {
    return this.roleProfileFields[this.currentUser?.role] || [];
  }

  onRoleChange(): void {
    const allFields = Object.values(this.roleProfileFields).flat().map(f => f.key);
    const unique = [...new Set(allFields)];
    unique.forEach(key => { delete this.currentUser[key]; });
  }

  tableSortField = 'firstName';
  tableSortDir: 'asc' | 'desc' = 'asc';

  // API Base URL
  private apiUrl = 'http://localhost:9099/auth/users';
  private registerUrl = 'http://localhost:9099/auth/register';

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.users = data;
        this.applyFilters();
        this.updateStats();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading users', err)
    });
  }

  updateStats(): void {
    this.totalUsers = this.users.length;
    this.adminsCount = this.users.filter(u => u.role === 'ADMIN').length;
  }

  get sortedFilteredUsers(): any[] {
    return [...this.filteredUsers].sort((a, b) => {
      let valA = a[this.tableSortField] ?? '';
      let valB = b[this.tableSortField] ?? '';
      if (typeof valA === 'string') valA = valA.toLowerCase();
      if (typeof valB === 'string') valB = valB.toLowerCase();
      if (valA < valB) return this.tableSortDir === 'asc' ? -1 : 1;
      if (valA > valB) return this.tableSortDir === 'asc' ? 1 : -1;
      return 0;
    });
  }

  sortTable(field: string): void {
    if (this.tableSortField === field) {
      this.tableSortDir = this.tableSortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.tableSortField = field;
      this.tableSortDir = 'asc';
    }
  }

  getRoleAttributeLabel(role: string): string {
    return this.roleAttributes[role]?.label || role;
  }

  getRoleFields(role: string): string[] {
    return this.roleAttributes[role]?.fields || [];
  }

  hasRoleAttributes(role: string): boolean {
    return (this.roleAttributes[role]?.fields?.length ?? 0) > 0;
  }

  hasRoleValues(user: any): boolean {
    const fields = this.getRoleFields(user?.role);
    return fields.some(field => user?.[field] !== undefined && user?.[field] !== null && user?.[field] !== '');
  }

  formatFieldLabel(field: string): string {
    const labels: Record<string, string> = {
      licenseNumber: 'License',
      isVerified: 'Verified',
      companyName: 'Company',
      shopDescription: 'Shop Desc.',
      businessName: 'Business',
      businessDescription: 'Description',
      region: 'Region',
      specialization: 'Specialization',
      certificationNumber: 'Certification #',
      yearsExperience: 'Years Exp.',
      coachingType: 'Coaching Type',
      certifications: 'Certifications',
      sponsorshipDescription: 'Sponsorship',
      industry: 'Industry',
      organizationName: 'Organization',
      partnershipDescription: 'Partnership',
      partnerType: 'Partner Type',
      experienceLevel: 'Level',
      preferredTerrain: 'Terrain'
    };
    return labels[field] || field;
  }

  applyFilters(): void {
    this.filteredUsers = this.users.filter(user => {
      const matchesSearch = !this.searchTerm ||
        (user.firstName + ' ' + user.lastName).toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase());

      const matchesRole = !this.roleFilter || user.role === this.roleFilter;

      return matchesSearch && matchesRole;
    });
    this.cdr.detectChanges();
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentUser = { role: 'COMPERS' }; // Reset form
    this.showModal = true;
  }

  openEditModal(user: any): void {
    this.isEditing = true;
    this.currentUser = { ...user }; // Clone user data
    // Ensure password field is empty for editing (security practice)
    this.currentUser.password = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  saveUser(): void {
    if (this.isEditing) {
      // Collect role-specific profile fields
      const roleProfileData: any = {};
      const fieldsForRole = this.roleProfileFields[this.currentUser.role] || [];
      fieldsForRole.forEach(f => {
          if (this.currentUser[f.key] !== undefined && this.currentUser[f.key] !== null && this.currentUser[f.key] !== '') {
              roleProfileData[f.key] = this.currentUser[f.key];
          }
      });

      const payload = {
          firstName:   this.currentUser.firstName,
          lastName:    this.currentUser.lastName,
          phoneNumber: this.currentUser.phoneNumber,
          role:        this.currentUser.role,
          enabled:     this.currentUser.enabled,
          ...roleProfileData
      };
      this.http.put(`${this.apiUrl}/${this.currentUser.id}`, payload).subscribe({
        next: () => { this.loadUsers(); this.closeModal(); },
        error: (err) => console.error('Error updating user', err)
      });
    } else {
      // Create User via register endpoint
      const fieldsForNewRole = this.roleProfileFields[this.currentUser.role] || [];
      const newUserProfileData: any = {};
      fieldsForNewRole.forEach(f => {
          if (this.currentUser[f.key] !== undefined && this.currentUser[f.key] !== '') {
              newUserProfileData[f.key] = this.currentUser[f.key];
          }
      });
      const newUserPayload = { ...this.currentUser, ...newUserProfileData };
      this.http.post(this.registerUrl, newUserPayload).subscribe({
        next: () => { this.loadUsers(); this.closeModal(); },
        error: (err) => console.error('Error creating user', err)
      });
    }
  }

  confirmDelete(id: string): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.deleteUser(id);
    }
  }

  deleteUser(id: string): void {
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (err) => console.error('Error deleting user', err)
    });
  }

  getInitials(firstName: string, lastName: string): string {
    return (firstName?.charAt(0) || '') + (lastName?.charAt(0) || '');
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'badge-danger';
      case 'COMPERS': return 'badge-success';
      case 'EQUIPEMENTPROVIEDERS': return 'badge-info';
      case 'COMPSITEOWNERS': return 'badge-warning';
      case 'DELIVERYAGENT': return 'badge-primary';
      case 'GUIDE': return 'badge-dark';
      case 'SPONSORS': return 'badge-secondary';
      case 'PARTENERS': return 'badge-light';
      default: return 'badge-secondary';
    }
  }
}
