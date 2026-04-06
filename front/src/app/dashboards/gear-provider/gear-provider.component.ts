import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';
import { User, UserRole } from '../../models/user.model';

@Component({
  selector: 'app-gear-provider',
  templateUrl: './gear-provider.component.html',
  styleUrls: ['./gear-provider.component.css']
})
export class GearProviderComponent implements OnInit {
  products: Product[] = [];
  categories: any[] = [];
  currentUser: User | null = null;
  UserRole = UserRole; // Make enum available in template
  isAdmin: boolean = false;

  // UI State
  showModal = false;
  saveError: string = '';
  saveLoading: boolean = false;
  isEditing = false;
  currentProduct: Partial<Product> = this.getEmptyProduct();

  warehouses: any[] = [];
  showWarehouseModal = false;
  isEditingWarehouse = false;
  currentWarehouse: any = { name: '', address: '', city: '', phone: '' };
  selectedWarehouseFilter: number | null = null;
  sortField: string = 'name';
  sortDir: 'asc' | 'desc' = 'asc';

  // Availability management
  showAvailabilityModal = false;
  selectedEquipmentForAvailability: any = null;
  blockedPeriods: any[] = [];
  bookedPeriods: any[] = [];
  newBlockedPeriod = { startDate: '', endDate: '', reason: '' };
  availabilityError = '';
  isLoadingPeriods = false;
  minBlockDate: string = new Date().toISOString().split('T')[0];

  constructor(
    private productService: ProductService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.cdr.detectChanges();
      this.isAdmin = (user?.role as string) === 'ADMIN';
      if (this.currentUser) {
        this.loadProducts();
        this.loadWarehouses();
        this.loadCategories();
      }
    });
  }

  loadProducts(): void {
    if (this.currentUser) {
      if ((this.currentUser.role as string) === 'ADMIN') {
        console.log('Admin loading all products');
        this.productService.getProducts().subscribe({
          next: (products) => {
            this.products = products;
          },
          error: (err) => {
            console.error('Error loading products for Admin:', err);
          }
        });
      } else if (this.currentUser.id) {
        console.log('Loading products for owner:', this.currentUser.id);
        this.productService.getProductsByProvider(this.currentUser.id).subscribe({
          next: (products) => {
            this.products = products;
          },
          error: (err) => {
            console.error('Error loading products for provider:', err);
          }
        });
      }
    } else {
      console.warn('Cannot load products: No current user');
    }
  }

  loadCategories(): void {
    this.productService.getCategories().subscribe(categories => {
      this.categories = categories;
    });
  }

  loadWarehouses(): void {
      if (!this.currentUser ||
          (this.currentUser.role as string) !== 'EQUIPEMENTPROVIEDERS') return;
      this.productService.getMyWarehouses().subscribe({
          next: w => this.warehouses = w,
          error: () => {}
      });
  }

  openAddWarehouseModal(): void {
      this.isEditingWarehouse = false;
      this.currentWarehouse = { name: '', address: '', city: '', phone: '' };
      this.showWarehouseModal = true;
  }

  openEditWarehouseModal(w: any): void {
      this.isEditingWarehouse = true;
      this.currentWarehouse = { ...w };
      this.showWarehouseModal = true;
  }

  closeWarehouseModal(): void { this.showWarehouseModal = false; }

  saveWarehouse(): void {
      const op = this.isEditingWarehouse
          ? this.productService.updateWarehouse(this.currentWarehouse.id, this.currentWarehouse)
          : this.productService.createWarehouse(this.currentWarehouse);
      op.subscribe({ next: () => { this.loadWarehouses(); this.closeWarehouseModal(); }, error: (err) => alert(err.error?.message || 'Failed to save warehouse.') });
  }

  deleteWarehouse(id: number): void {
      if (!confirm('Delete this warehouse?')) return;
      this.productService.deleteWarehouse(id).subscribe({ next: () => this.loadWarehouses(), error: (err) => alert(err.error?.message || 'Failed.') });
  }

  get filteredProducts() {
      if (this.selectedWarehouseFilter == null) return this.products;
      return this.products.filter(p => p.warehouseId === this.selectedWarehouseFilter);
  }

  get sortedProducts(): Product[] {
      let list = this.selectedWarehouseFilter != null
          ? this.products.filter(p => p.warehouseId === this.selectedWarehouseFilter)
          : [...this.products];

      list.sort((a, b) => {
          let valA: any;
          let valB: any;

          switch (this.sortField) {
              case 'name':
                  valA = a.name?.toLowerCase();
                  valB = b.name?.toLowerCase();
                  break;
              case 'stock':
                  valA = a.stock ?? 0;
                  valB = b.stock ?? 0;
                  break;
              case 'price':
                  valA = a.purchasePrice ?? a.pricePerDay ?? a.price ?? 0;
                  valB = b.purchasePrice ?? b.pricePerDay ?? b.price ?? 0;
                  break;
              case 'category':
                  valA = a.category?.toLowerCase();
                  valB = b.category?.toLowerCase();
                  break;
              case 'warehouse':
                  valA = this.getWarehouseName(a.warehouseId)?.toLowerCase();
                  valB = this.getWarehouseName(b.warehouseId)?.toLowerCase();
                  break;
              default:
                  valA = a.name?.toLowerCase();
                  valB = b.name?.toLowerCase();
          }

          if (valA == null) return 1;
          if (valB == null) return -1;
          if (valA < valB) return this.sortDir === 'asc' ? -1 : 1;
          if (valA > valB) return this.sortDir === 'asc' ? 1 : -1;
          return 0;
      });

      return list;
  }

  sortByField(field: string): void {
      if (this.sortField === field) {
          this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
      } else {
          this.sortField = field;
          this.sortDir = 'asc';
      }
  }

  toggleSortDir(): void {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
  }

  applySort(): void {
      // Intentionally empty: the getter re-evaluates via Angular change detection.
  }

  getWarehouseName(warehouseId?: number): string {
      if (!warehouseId) return '—';
      const w = this.warehouses.find(w => w.id === warehouseId);
      return w ? w.name : '—';
  }

  getEmptyProduct(): Partial<Product> {
    return {
      name: '',
      description: '',
      pricePerDay: undefined,
      purchasePrice: undefined,
      availableForRent: false,
      availableForSale: false,
      condition: '',
      specifications: '',
      weight: undefined,
      categoryId: undefined,
      imageUrl: 'https://images.unsplash.com/photo-1510672981848-a1c4f1cb5ccf?w=500',
      stock: 0,
      providerId: this.currentUser?.id || '',
      warehouseId: undefined
    };
  }

  openAddModal(): void {
    this.isEditing = false;
    this.currentProduct = this.getEmptyProduct();
    this.showModal = true;
  }

  openEditModal(product: Product): void {
    this.isEditing = true;
    this.currentProduct = { ...product };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  saveProduct(): void {
    this.saveError = '';

    // Run client-side validation before sending to backend
    const validationError = this.productService.validateProductPayload(this.currentProduct);
    if (validationError) {
        this.saveError = validationError;
        return;
    }

    this.saveLoading = true;
    const operation = this.isEditing ? 'update' : 'add';
    const request = this.isEditing
        ? this.productService.updateProduct(this.currentProduct.id!, this.currentProduct)
        : this.productService.addProduct(this.currentProduct as any);

    console.log(`Attempting to ${operation} product:`, this.currentProduct);

    request.subscribe({
        next: (res) => {
            console.log(`Product ${operation}ed successfully:`, res);
            this.saveLoading = false;
            this.loadProducts();
            this.closeModal();
        },
        error: (err) => {
            this.saveLoading = false;
            console.error(`SERVER ERROR (${operation} product):`, err);

            // Show a meaningful error from the server, or a fallback
            if (err.status === 400) {
                // Backend returns validation errors as JSON or plain text
                const serverMsg = err.error?.message
                    || (typeof err.error === 'string' ? err.error : null)
                    || 'Please check all required fields.';
                this.saveError = `Validation error: ${serverMsg}`;
            } else if (err.status === 403) {
                this.saveError = 'Permission denied. You can only edit your own products.';
            } else if (err.status === 409) {
                this.saveError = err.error?.message || 'Conflict error.';
            } else {
                this.saveError = `Server error (${err.status}). Please try again.`;
            }
        }
    });
}

  deleteProduct(id: string): void {
    if (confirm('Are you sure you want to delete this gear?')) {
      this.productService.deleteProduct(id).subscribe(() => {
        this.loadProducts();
      });
    }
  }

  // Stats Helpers
  get totalItems(): number {
    return this.products.length;
  }

  get totalStock(): number {
    return this.products.reduce((acc, p) => acc + p.stock, 0);
  }

  openAvailability(product: any) {
    this.selectedEquipmentForAvailability = product;
    this.showAvailabilityModal = true;
    this.loadAvailabilityData(product.id);
  }

  closeAvailability() {
    this.showAvailabilityModal = false;
    this.selectedEquipmentForAvailability = null;
    this.blockedPeriods = [];
    this.bookedPeriods = [];
    this.newBlockedPeriod = { startDate: '', endDate: '', reason: '' };
    this.availabilityError = '';
  }

  loadAvailabilityData(equipmentId: any) {
    this.isLoadingPeriods = true;

    // Load manually blocked periods (provider-managed)
    this.productService.getBlockedPeriods(equipmentId).subscribe({
      next: p => { this.blockedPeriods = p; },
      error: () => {}
    });

    // Load all unavailable periods and extract BOOKED ones (camper orders)
    this.productService.getUnavailablePeriods(equipmentId).subscribe({
      next: periods => {
        this.bookedPeriods = periods.filter(p => p.type === 'BOOKED');
        this.isLoadingPeriods = false;
      },
      error: () => { this.isLoadingPeriods = false; }
    });
  }

  addBlockedPeriod() {
    if (!this.newBlockedPeriod.startDate || !this.newBlockedPeriod.endDate) {
      this.availabilityError = 'Start and end dates are required.'; return;
    }
    this.availabilityError = '';
    this.productService.addBlockedPeriod({
      equipmentId: parseInt(this.selectedEquipmentForAvailability.id, 10),
      ...this.newBlockedPeriod
    }).subscribe({
      next: () => {
        this.newBlockedPeriod = { startDate: '', endDate: '', reason: '' };
        this.loadAvailabilityData(this.selectedEquipmentForAvailability.id);
      },
      error: () => this.availabilityError = 'Failed to add blocked period.'
    });
  }

  removeBlockedPeriod(id: number) {
    this.productService.deleteBlockedPeriod(id).subscribe({
      next: () => this.loadAvailabilityData(
        this.selectedEquipmentForAvailability.id),
      error: () => this.availabilityError = 'Failed to remove period.'
    });
  }
}
