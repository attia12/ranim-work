import { Component, OnInit } from '@angular/core';
import { DeliveryService, Delivery, Vehicle } from '../../services/delivery.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-backoffice-delivery',
  templateUrl: './delivery.component.html',
  styleUrls: ['./delivery.component.css']
})
export class BackofficeDeliveryComponent implements OnInit {
  availableDeliveries: Delivery[] = [];
  myDeliveries: Delivery[] = [];
  currentUser: User | null = null;
  activeTab: 'available' | 'managed' | 'vehicles' = 'available';
  sortBy: 'date' | 'price' = 'date';
  totalEarnings: number = 0;
  selectedDelivery: Delivery | null = null;

  // Vehicle management
  vehicles: Vehicle[] = [];
  showVehicleModal = false;
  isEditingVehicle = false;
  vehicleSaveError = '';
  vehicleSaving = false;
  currentVehicle: Partial<Vehicle> = this.emptyVehicle();

  vehicleTypes = ['MOTORCYCLE', 'CAR', 'VAN', 'TRUCK'];
  availabilityStatuses = ['AVAILABLE', 'BUSY', 'OFFLINE'];

  constructor(
    private deliveryService: DeliveryService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadDeliveries();
        this.loadVehicles();
      }
    });
    this.route.queryParams.subscribe(params => {
      if (params['tab']) this.activeTab = params['tab'];
    });
  }

  // ── Deliveries ─────────────────────────────────────────────────────────────

  loadDeliveries(): void {
    this.deliveryService.getAvailableDeliveries().subscribe(d => {
      this.availableDeliveries = this.sortDeliveries(d);
    });
    if (this.currentUser) {
      this.deliveryService.getMyDeliveries(this.currentUser.id).subscribe(d => {
        this.myDeliveries = this.sortDeliveries(d);
        this.calculateEarnings();
      });
    }
  }

  private sortDeliveries(deliveries: Delivery[]): Delivery[] {
    return deliveries.sort((a, b) =>
      this.sortBy === 'date'
        ? new Date(b.date).getTime() - new Date(a.date).getTime()
        : b.price - a.price
    );
  }

  calculateEarnings(): void {
    this.totalEarnings = this.myDeliveries
      .filter(d => d.status === 'DELIVERED')
      .reduce((sum, d) => sum + d.price, 0);
  }

  onSortChange(criteria: 'date' | 'price'): void {
    this.sortBy = criteria;
    this.loadDeliveries();
  }

  selectDelivery(delivery: Delivery): void { this.selectedDelivery = delivery; }
  closeDetails(): void { this.selectedDelivery = null; }

  takeDelivery(id: string): void {
    const delivery = this.availableDeliveries.find(d => d.id === id);
    if (!delivery || delivery.status !== 'PENDING_ASSIGNMENT') { this.loadDeliveries(); return; }
    if (this.currentUser) {
      this.deliveryService.takeDelivery(id, this.currentUser.id).subscribe({
        next: () => this.loadDeliveries(),
        error: (err) => {
          const msg = err.error?.message || 'Could not claim delivery.';
          if (msg.includes('no available vehicle') || msg.includes('vehicle')) {
            // Guide user to register a vehicle
            if (confirm(msg + '\n\nWould you like to go to your Vehicles tab to register one?')) {
              this.activeTab = 'vehicles';
            }
          } else {
            alert(msg);
          }
          this.loadDeliveries();
        }
      });
    }
  }

  declineDelivery(id: string): void {
    this.deliveryService.declineDelivery(id).subscribe(() => this.loadDeliveries());
  }

  getNextStatus(current: string): string | null {
    const chain: Record<string, string> = {
      'ASSIGNED': 'AT_PICKUP', 'AT_PICKUP': 'PICKED_UP',
      'PICKED_UP': 'IN_TRANSIT', 'IN_TRANSIT': 'DELIVERED'
    };
    return chain[current] || null;
  }

  getNextStatusLabel(current: string): string {
    const labels: Record<string, string> = {
      'ASSIGNED': 'Arrived at Pickup', 'AT_PICKUP': 'Items Picked Up',
      'PICKED_UP': 'In Transit', 'IN_TRANSIT': 'Mark Delivered'
    };
    return labels[current] || 'Update';
  }

  getStatusBadgeClass(status: string): string {
    const classes: Record<string, string> = {
      'ASSIGNED': 'badge-primary', 'AT_PICKUP': 'badge-info',
      'PICKED_UP': 'badge-warning', 'IN_TRANSIT': 'badge-warning',
      'DELIVERED': 'badge-success', 'FAILED': 'badge-danger', 'RETURNED': 'badge-secondary'
    };
    return classes[status] || 'badge-secondary';
  }

  advanceStatus(deliveryId: string, currentStatus: string): void {
    const next = this.getNextStatus(currentStatus);
    if (!next) return;
    this.deliveryService.updateStatus(deliveryId, next).subscribe({
      next: () => this.loadDeliveries(),
      error: (err) => alert(err.error?.message || 'Failed to update status.')
    });
  }

  completeDelivery(id: string): void {
    this.deliveryService.updateStatus(id, 'DELIVERED').subscribe(() => this.loadDeliveries());
  }

  // ── Vehicles ───────────────────────────────────────────────────────────────

  loadVehicles(): void {
    this.deliveryService.getMyVehicles().subscribe({
      next: v => this.vehicles = v,
      error: () => this.vehicles = []
    });
  }

  emptyVehicle(): Partial<Vehicle> {
    return {
      type: 'CAR', brand: '', model: '', year: new Date().getFullYear(),
      color: '', plate: '', maxWeight: undefined, maxConcurrentDeliveries: 1,
      serviceRadius: undefined, insuranceExpiryDate: ''
    };
  }

  openAddVehicleModal(): void {
    this.isEditingVehicle = false;
    this.currentVehicle = this.emptyVehicle();
    this.vehicleSaveError = '';
    this.showVehicleModal = true;
  }

  openEditVehicleModal(v: Vehicle): void {
    this.isEditingVehicle = true;
    this.currentVehicle = {
      ...v,
      insuranceExpiryDate: v.insuranceExpiryDate
        ? new Date(v.insuranceExpiryDate).toISOString().slice(0, 16)
        : ''
    };
    this.vehicleSaveError = '';
    this.showVehicleModal = true;
  }

  closeVehicleModal(): void {
    this.showVehicleModal = false;
    this.vehicleSaveError = '';
  }

  validateVehicle(): string | null {
    if (!this.currentVehicle.type) return 'Vehicle type is required.';
    if (!this.currentVehicle.brand?.trim()) return 'Brand is required.';
    if (!this.currentVehicle.model?.trim()) return 'Model is required.';
    if (!this.currentVehicle.year || this.currentVehicle.year < 1900) return 'Valid year is required.';
    const platePat = /^[A-Z0-9 \-]{2,20}$/;
    if (!this.currentVehicle.plate?.trim()) return 'Plate number is required.';
    if (!platePat.test(this.currentVehicle.plate.toUpperCase()))
      return 'Invalid plate format (uppercase letters, digits, spaces, hyphens only).';
    if (!this.currentVehicle.maxWeight || this.currentVehicle.maxWeight <= 0)
      return 'Max weight must be a positive number.';
    if (!this.currentVehicle.maxConcurrentDeliveries || this.currentVehicle.maxConcurrentDeliveries < 1)
      return 'Max concurrent deliveries must be at least 1.';
    if (this.currentVehicle.insuranceExpiryDate) {
      const d = new Date(this.currentVehicle.insuranceExpiryDate);
      if (isNaN(d.getTime()) || d <= new Date())
        return 'Insurance expiry date must be in the future.';
    }
    return null;
  }

  saveVehicle(): void {
    this.vehicleSaveError = '';
    const err = this.validateVehicle();
    if (err) { this.vehicleSaveError = err; return; }

    this.vehicleSaving = true;
    const payload = {
      ...this.currentVehicle,
      plate: this.currentVehicle.plate?.toUpperCase(),
      insuranceExpiryDate: this.currentVehicle.insuranceExpiryDate
        ? new Date(this.currentVehicle.insuranceExpiryDate).toISOString()
        : undefined
    };

    const obs = this.isEditingVehicle
      ? this.deliveryService.updateVehicle(this.currentVehicle.id!, payload)
      : this.deliveryService.registerVehicle(payload);

    obs.subscribe({
      next: () => {
        this.vehicleSaving = false;
        this.closeVehicleModal();
        this.loadVehicles();
      },
      error: (e) => {
        this.vehicleSaving = false;
        this.vehicleSaveError = e.error?.message || `Server error (${e.status}).`;
      }
    });
  }

  deleteVehicle(id: number): void {
    if (!confirm('Delete this vehicle? This cannot be undone.')) return;
    this.deliveryService.deleteVehicle(id).subscribe({
      next: () => this.loadVehicles(),
      error: (e) => alert(e.error?.message || 'Failed to delete vehicle.')
    });
  }

  setAvailability(id: number, status: string): void {
    this.deliveryService.updateVehicleAvailability(id, status).subscribe({
      next: () => this.loadVehicles(),
      error: (e) => alert(e.error?.message || 'Failed to update availability.')
    });
  }

  getAvailabilityBadge(status: string): string {
    return status === 'AVAILABLE' ? 'badge-success'
         : status === 'BUSY'      ? 'badge-warning'
         : 'badge-secondary';
  }

  getVehicleIcon(type: string): string {
    return type === 'MOTORCYCLE' ? 'fa-motorcycle'
         : type === 'VAN'        ? 'fa-shuttle-van'
         : type === 'TRUCK'      ? 'fa-truck'
         : 'fa-car';
  }

  get hasAvailableVehicle(): boolean {
    return this.vehicles.some(v => v.availabilityStatus === 'AVAILABLE');
  }
}
