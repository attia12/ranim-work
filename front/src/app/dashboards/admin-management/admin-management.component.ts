import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { ForumService } from '../../services/forum.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-management',
  templateUrl: './admin-management.component.html',
  styleUrls: ['./admin-management.component.css'],
  standalone: false
})
export class AdminManagementComponent implements OnInit {
  type: string = '';
  title: string = '';
  items: any[] = [];
  loading: boolean = false;
  columns: string[] = [];
  licenses: any[] = [];
  licenseFilter: 'ALL' | 'VERIFIED' | 'PENDING' = 'ALL';

  // Category modal
  showCategoryModal = false;
  isEditingCategory = false;
  currentCategory: any = { name: '', description: '' };
  isSavingCategory = false;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private forumService: ForumService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) { }

  get filteredLicenses(): any[] {
    if (this.licenseFilter === 'VERIFIED') return this.licenses.filter(l => l.isVerified);
    if (this.licenseFilter === 'PENDING') return this.licenses.filter(l => !l.isVerified);
    return this.licenses;
  }

  get pendingLicensesCount(): number {
    return this.licenses.filter(l => !l.isVerified).length;
  }

  get verifiedLicensesCount(): number {
    return this.licenses.filter(l => l.isVerified).length;
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.type = data['type'];
      this.setupView();
      this.loadData();
    });
  }

  setupView(): void {
    switch (this.type) {
      case 'categories':
        this.title = 'Equipment Categories';
        this.columns = ['Name', 'Description', 'Actions'];
        break;
      case 'forum-categories':
        this.title = 'Forum Categories';
        this.columns = ['Name', 'Description', 'Actions'];
        break;
      case 'gear-reviews':
        this.title = 'Gear Reviews';
        this.columns = ['Product', 'Rating', 'Comment', 'User', 'Actions'];
        break;
      case 'payments':
        this.title = 'Platform Payments';
        this.columns = ['Ref', 'Amount', 'Status', 'Date', 'Actions'];
        break;
      case 'delivery-reviews':
        this.title = 'Delivery Ratings';
        this.columns = ['Delivery ID', 'Rating', 'Comment', 'Actions'];
        break;
      case 'orders':
        this.title = 'All Platform Orders';
        this.columns = ['Order #', 'Customer', 'Total', 'Status', 'Date', 'Actions'];
        break;
      case 'licenses':
        this.title = 'License Management';
        this.columns = ['Agent', 'License Number', 'Status', 'Since', 'Action'];
        this.loadLicenses();
        break;
    }
  }

  loadData(): void {
    this.loading = true;
    let request;

    switch (this.type) {
      case 'categories':
        request = this.productService.getCategories();
        break;
      case 'forum-categories':
        request = this.forumService.getAllCategories();
        break;
      case 'gear-reviews':
        request = this.http.get<any[]>(`${environment.apiUrl}/api/marketplace/reviews`);
        break;
      case 'payments':
        request = this.http.get<any[]>(`${environment.apiUrl}/api/marketplace/payments`);
        break;
      case 'delivery-reviews':
        request = this.http.get<any[]>(`${environment.apiUrl}/api/delivery/ratings`);
        break;
      case 'orders':
        request = this.http.get<any[]>(`${environment.apiUrl}/api/marketplace/orders`);
        break;
      case 'licenses':
        this.loading = false;
        return;
    }

    if (request) {
      request.subscribe({
        next: (res) => {
          this.items = res;
          this.loading = false;
        },
        error: (err) => {
          console.error(`Error loading ${this.type}:`, err);
          this.loading = false;
        }
      });
    } else {
      this.loading = false;
    }
  }

  loadLicenses(): void {
    this.loading = true;
    this.http.get<any[]>(`${environment.apiUrl}/auth/profile/agents`).subscribe({
      next: data => {
        this.licenses = data || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Error loading licenses', err);
        this.loading = false;
      }
    });
  }

  verifyLicense(userId: number): void {
    this.http.put(`${environment.apiUrl}/auth/profile/agent/${userId}/verify`, {}).subscribe({
      next: () => this.loadLicenses(),
      error: err => console.error('Error verifying license', err)
    });
  }

  deleteItem(id: any): void {
    if (confirm('Are you sure you want to delete this item?')) {
      let url = '';
      switch (this.type) {
        case 'categories': url = `${environment.apiUrl}/api/marketplace/categories/${id}`; break;
        case 'forum-categories': url = `${environment.apiUrl}/categories/${id}`; break;
        case 'gear-reviews': url = `${environment.apiUrl}/api/marketplace/reviews/${id}`; break;
        case 'payments': url = `${environment.apiUrl}/api/marketplace/payments/${id}`; break;
        case 'delivery-reviews': url = `${environment.apiUrl}/api/delivery/ratings/${id}`; break;
      }

      this.http.delete(url).subscribe(() => {
        alert('Deleted successfully');
        this.loadData();
      });
    }
  }

  openAddCategoryModal(): void {
    this.isEditingCategory = false;
    this.currentCategory = { name: '', description: '' };
    this.showCategoryModal = true;
  }

  openEditCategoryModal(item: any): void {
    this.isEditingCategory = true;
    this.currentCategory = { id: item.id, name: item.name, description: item.description || '' };
    this.showCategoryModal = true;
  }

  closeCategoryModal(): void {
    this.showCategoryModal = false;
  }

  saveCategory(): void {
    if (!this.currentCategory.name?.trim()) return;
    this.isSavingCategory = true;

    let obs;
    if (this.type === 'forum-categories') {
      obs = this.isEditingCategory
        ? this.http.put(`${environment.apiUrl}/categories/${this.currentCategory.id}`,
          { name: this.currentCategory.name, description: this.currentCategory.description })
        : this.forumService.createCategory({ name: this.currentCategory.name, description: this.currentCategory.description });
    } else {
      obs = this.isEditingCategory
        ? this.http.put(`${environment.apiUrl}/api/marketplace/categories/${this.currentCategory.id}`,
          { name: this.currentCategory.name, description: this.currentCategory.description })
        : this.productService.createCategory(this.currentCategory.name, this.currentCategory.description);
    }

    obs.subscribe({
      next: () => {
        this.isSavingCategory = false;
        this.closeCategoryModal();
        this.loadData();
      },
      error: (err) => {
        console.error('Error saving category', err);
        this.isSavingCategory = false;
      }
    });
  }
}
