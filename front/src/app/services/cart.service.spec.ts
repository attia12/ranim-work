import { TestBed } from '@angular/core/testing';
import { CartService } from './cart.service';
import { NotificationService } from './notification.service';
import { PLATFORM_ID } from '@angular/core';
import { Product } from '../models/product.model';

const mockProduct: Product = {
  id: '1',
  name: 'Tent Pro',
  description: 'A great tent',
  price: 50,
  pricePerDay: 10,
  purchasePrice: 50,
  availableForRent: true,
  availableForSale: true,
  imageUrl: '',
  category: 'Tents',
  providerId: 'p1',
  stock: 5,
  rating: 4,
  reviews: []
};

describe('CartService', () => {
  let service: CartService;
  let notifSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(() => {
    notifSpy = jasmine.createSpyObj('NotificationService', ['push', 'initForUser', 'clearSession']);

    TestBed.configureTestingModule({
      providers: [
        CartService,
        { provide: NotificationService, useValue: notifSpy },
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    });

    service = TestBed.inject(CartService);
    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  // ── addToCart ──────────────────────────────────────────────────────────────

  it('should add a new item to cart', () => {
    service.addToCart(mockProduct, 'BUY');
    expect(service.getCartCount()).toBe(1);
  });

  it('should increment quantity when same product added twice', () => {
    service.addToCart(mockProduct, 'BUY');
    service.addToCart(mockProduct, 'BUY');
    expect(service.getCartCount()).toBe(2);
  });

  it('should add as separate items for different transaction types', () => {
    service.addToCart(mockProduct, 'BUY');
    service.addToCart(mockProduct, 'RENT', 3);
    expect(service.getCartCount()).toBe(2);
  });

  it('should call notifService.push when item is added', () => {
    service.addToCart(mockProduct, 'BUY');
    expect(notifSpy.push).toHaveBeenCalled();
  });

  // ── removeFromCart ─────────────────────────────────────────────────────────

  it('should remove item from cart', () => {
    service.addToCart(mockProduct, 'BUY');
    service.removeFromCart('1', 'BUY');
    expect(service.getCartCount()).toBe(0);
  });

  it('should not remove items with different transaction type', () => {
    service.addToCart(mockProduct, 'BUY');
    service.removeFromCart('1', 'RENT');
    expect(service.getCartCount()).toBe(1);
  });

  // ── updateQuantity ─────────────────────────────────────────────────────────

  it('should update item quantity', () => {
    service.addToCart(mockProduct, 'BUY');
    service.updateQuantity('1', 5);
    expect(service.getCartCount()).toBe(5);
  });

  it('should not update if product id does not exist', () => {
    service.addToCart(mockProduct, 'BUY');
    service.updateQuantity('999', 10);
    expect(service.getCartCount()).toBe(1);
  });

  // ── clearCart ──────────────────────────────────────────────────────────────

  it('should clear all items from cart', () => {
    service.addToCart(mockProduct, 'BUY');
    service.clearCart();
    expect(service.getCartCount()).toBe(0);
  });

  // ── clearCartOnLogout ──────────────────────────────────────────────────────

  it('should clear cart on logout', () => {
    service.addToCart(mockProduct, 'BUY');
    service.clearCartOnLogout();
    expect(service.getCartCount()).toBe(0);
  });

  // ── getCartCount ───────────────────────────────────────────────────────────

  it('should return 0 for empty cart', () => {
    expect(service.getCartCount()).toBe(0);
  });

  it('should return correct total quantity across multiple items', () => {
    const product2: Product = { ...mockProduct, id: '2', name: 'Sleeping Bag' };
    service.addToCart(mockProduct, 'BUY');
    service.addToCart(mockProduct, 'BUY');
    service.addToCart(product2, 'BUY');
    expect(service.getCartCount()).toBe(3);
  });
});
