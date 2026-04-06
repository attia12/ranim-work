import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { Inject, PLATFORM_ID } from '@angular/core';
import { Product } from '../models/product.model';
import { NotificationService } from './notification.service';

export interface CartItem extends Product {
    quantity: number;
    transactionType: 'RENT' | 'BUY';
    rentalDays?: number;
    rentalStartDate?: string;  // ISO date string e.g. "2025-08-01"
    rentalEndDate?: string;
}

@Injectable({
    providedIn: 'root'
})
export class CartService {
    private itemsSubject = new BehaviorSubject<CartItem[]>([]);
    public items$ = this.itemsSubject.asObservable();

    constructor(@Inject(PLATFORM_ID) private platformId: Object, private notifService: NotificationService) {
        if (isPlatformBrowser(this.platformId)) {
            const savedCart = localStorage.getItem(this.cartKey());
            if (savedCart) {
                this.itemsSubject.next(JSON.parse(savedCart));
            }
        }
    }

    private cartKey(): string {
        try {
            const raw = localStorage.getItem('currentUser');
            if (raw) {
                const user = JSON.parse(raw);
                return `cart_${user.id || 'guest'}`;
            }
        } catch { }
        return 'cart_guest';
    }

    clearCartOnLogout() {
        this.itemsSubject.next([]);
        if (isPlatformBrowser(this.platformId)) {
            localStorage.removeItem(this.cartKey());
        }
    }

    addToCart(product: Product, transactionType: 'RENT' | 'BUY' = 'BUY', rentalDays?: number, rentalStartDate?: string, rentalEndDate?: string) {
        const currentItems = this.itemsSubject.value;
        const key = `${product.id}-${transactionType}-${rentalDays ?? 0}`;
        const existingItem = currentItems.find(
            item => item.id === product.id &&
                item.transactionType === transactionType &&
                item.rentalDays === rentalDays
        );

        if (existingItem) {
            existingItem.quantity += 1;
            this.itemsSubject.next([...currentItems]);
        } else {
            this.itemsSubject.next([...currentItems, {
                ...product,
                quantity: 1,
                transactionType,
                rentalDays,
                rentalStartDate,
                rentalEndDate
            }]);
        }
        
        this.notifService.push({
          type: 'cart',
          title: 'Added to Cart',
          message: `${product.name} was added to your cart`,
          icon: 'fa-shopping-cart',
          iconColor: '#2d5016',
          link: '/cart'
        });

        this.saveCart();
    }

    updateQuantity(productId: string, quantity: number) {
        const currentItems = this.itemsSubject.value;
        const item = currentItems.find(i => i.id === productId);
        if (item) {
            item.quantity = quantity;
            this.itemsSubject.next([...currentItems]);
            this.saveCart();
        }
    }

    removeFromCart(productId: string, transactionType?: string, rentalDays?: number) {
        this.itemsSubject.next(
            this.itemsSubject.value.filter(item =>
                !(item.id === productId &&
                    (!transactionType || item.transactionType === transactionType) &&
                    (!rentalDays || item.rentalDays === rentalDays))
            )
        );
        this.saveCart();
    }

    clearCart() {
        this.itemsSubject.next([]);
        this.saveCart();
    }

    private saveCart() {
        if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem(this.cartKey(), JSON.stringify(this.itemsSubject.value));
        }
    }

    getCartCount(): number {
        return this.itemsSubject.value.reduce((acc, item) => acc + item.quantity, 0);
    }
}
