import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { NotificationService } from './notification.service';

export interface DeliveryItem {
    name: string;
    quantity: number;
    weight: number;
}

export interface Delivery {
    id: string;
    orderId: string;
    customerName: string;
    address: string;
    status: string;
    personnelId?: string;
    date: Date;
    price: number;
    items: DeliveryItem[];
    weight: number;
    instructions?: string;
}

export interface Vehicle {
    id?: number;
    type: string;
    brand: string;
    model: string;
    year: number;
    color?: string;
    plate: string;
    maxWeight: number;
    maxConcurrentDeliveries: number;
    serviceRadius?: number;
    insuranceExpiryDate?: string;
    availabilityStatus?: string;
    isVerified?: boolean;
    currentDeliveriesCount?: number;
    ownerId?: number;
    ownerFullName?: string;
}

@Injectable({
    providedIn: 'root'
})
export class DeliveryService {
    private apiUrl = `${environment.apiUrl}/api/delivery/deliveries`;
    private pollingInterval: any = null;
    private lastStatuses = new Map<string, string>();

    private deliveryStatusLabels: Record<string, { label: string; icon: string; color: string }> = {
        PENDING_ASSIGNMENT: { label: 'Waiting for a delivery agent',      icon: 'fa-clock',        color: '#f59e0b' },
        ASSIGNED:           { label: 'Agent assigned to your order',      icon: 'fa-user-check',   color: '#3b82f6' },
        PICKED_UP:          { label: 'Order picked up by agent',          icon: 'fa-box',          color: '#8b5cf6' },
        IN_TRANSIT:         { label: 'Your order is on the way!',         icon: 'fa-truck',        color: '#2d5016' },
        DELIVERED:          { label: 'Order delivered successfully!',     icon: 'fa-check-circle', color: '#10b981' },
        FAILED:             { label: 'Delivery failed — contact support', icon: 'fa-times-circle', color: '#ef4444' },
    };

    constructor(private http: HttpClient, private notifService: NotificationService) {}

    // ✅ Poll uniquement pour DELIVERYAGENT
    startPolling(userId: string, role?: string): void {
        if (role && role !== 'DELIVERYAGENT' && role !== 'ROLE_DELIVERYAGENT') {
            console.log('⛔ Polling désactivé pour le rôle:', role);
            return;
        }

        // ✅ Éviter les doublons
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }

        this.pollingInterval = setInterval(() => {
            this.http.get<any[]>(`${this.apiUrl}/my`).subscribe({
                next: (deliveries) => {
                    deliveries.forEach(d => {
                        const key = d.id.toString();
                        const prev = this.lastStatuses.get(key);
                        const curr = d.status;
                        if (prev !== undefined && prev !== curr) {
                            const meta = this.deliveryStatusLabels[curr]
                                || { label: curr, icon: 'fa-truck', color: '#6b7280' };
                            const orderIdForLink = d.orderId || d.order?.id || d.id;
                            this.notifService.push({
                                type: 'delivery',
                                title: 'Delivery Update 📦',
                                message: meta.label,
                                icon: meta.icon,
                                iconColor: meta.color,
                                link: `/orders/${orderIdForLink}`
                            });
                        }
                        this.lastStatuses.set(key, curr);
                    });
                },
                error: () => {}
            });
        }, 30000);
    }

    // ✅ Arrêter le polling proprement
    stopPolling(): void {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
        }
    }

    private mapToDelivery(item: any): Delivery {
        return {
            id: item.id.toString(),
            orderId: item.orderNumber || item.orderId?.toString() || '',
            customerName: item.camperFullName || 'Customer',
            address: item.deliveryAddress || 'No Address Provided',
            status: item.status,
            personnelId: item.vehicleId?.toString(),
            date: new Date(item.createdAt || item.scheduledPickupTime || Date.now()),
            price: item.earningAmount || item.actualCost || 0,
            items: [],
            weight: 0,
            instructions: item.deliveryNotes
        };
    }

    getAvailableDeliveries(): Observable<Delivery[]> {
        return this.http.get<any[]>(`${this.apiUrl}/available`).pipe(
            map(items => items.map(this.mapToDelivery))
        );
    }

    getMyDeliveries(personnelId: string): Observable<Delivery[]> {
        return this.http.get<any[]>(`${this.apiUrl}/agent`).pipe(
            map(items => items.map(this.mapToDelivery))
        );
    }

    getLatestDelivery(userId: string): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/latest`);
    }

    takeDelivery(deliveryId: string, personnelId: string): Observable<any> {
        return this.http.put<any>(`${this.apiUrl}/${deliveryId}/claim`, {});
    }

    declineDelivery(deliveryId: string): Observable<any> {
        return this.http.get<any[]>(`${this.apiUrl}/available`);
    }

    updateStatus(deliveryId: string, status: string): Observable<boolean> {
        return this.http.put<any>(`${this.apiUrl}/${deliveryId}/status`, { status: status }).pipe(
            map(res => !!res)
        );
    }

    private vehicleUrl = `${environment.apiUrl}/api/delivery/vehicles`;

    getMyVehicles(): Observable<Vehicle[]> {
        return this.http.get<Vehicle[]>(`${this.vehicleUrl}/my`);
    }

    registerVehicle(vehicle: Partial<Vehicle>): Observable<Vehicle> {
        return this.http.post<Vehicle>(this.vehicleUrl, vehicle);
    }

    updateVehicle(id: number, vehicle: Partial<Vehicle>): Observable<Vehicle> {
        return this.http.put<Vehicle>(`${this.vehicleUrl}/${id}`, vehicle);
    }

    deleteVehicle(id: number): Observable<any> {
        return this.http.delete(`${this.vehicleUrl}/${id}`, { responseType: 'text' });
    }

    updateVehicleAvailability(id: number, status: string): Observable<Vehicle> {
        return this.http.put<Vehicle>(`${this.vehicleUrl}/${id}/availability?status=${status}`, {});
    }

    private ratingUrl = `${environment.apiUrl}/api/delivery/ratings`;

    getRatingByDelivery(deliveryId: number): Observable<any> {
        return this.http.get<any>(`${this.ratingUrl}/delivery/${deliveryId}`);
    }

    createRating(deliveryId: number, rating: number, comment: string): Observable<any> {
        return this.http.post<any>(this.ratingUrl, { deliveryId, rating, comment });
    }

    updateRating(ratingId: number, rating: number, comment: string): Observable<any> {
        return this.http.put<any>(`${this.ratingUrl}/${ratingId}`, { rating, comment });
    }

    deleteRating(ratingId: number): Observable<any> {
        return this.http.delete(`${this.ratingUrl}/${ratingId}`, { responseType: 'text' });
    }
}
