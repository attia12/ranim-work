import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Product } from '../models/product.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/api/marketplace/equipment`;
  private categoryUrl = `${environment.apiUrl}/api/marketplace/categories`;
  private warehouseUrl = `${environment.apiUrl}/auth/profile/provider/warehouses`;

  constructor(private http: HttpClient) { }

  getMyWarehouses(): Observable<any[]> {
    return this.http.get<any[]>(this.warehouseUrl);
  }

  createWarehouse(data: { name: string; address: string; city: string; phone: string }): Observable<any> {
    return this.http.post<any>(this.warehouseUrl, data);
  }

  updateWarehouse(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.warehouseUrl}/${id}`, data);
  }

  deleteWarehouse(id: number): Observable<any> {
    return this.http.delete(`${this.warehouseUrl}/${id}`, { responseType: 'text' });
  }

  getProducts(): Observable<Product[]> {
    return this.http.get<any[]>(this.apiUrl).pipe(
      map(items => (Array.isArray(items) ? items : []).map(item => this.mapToProduct(item)))
    );
  }

  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(this.categoryUrl);
  }

  createCategory(name: string, description: string): Observable<any> {
    return this.http.post<any>(this.categoryUrl, { name, description });
  }

  getProductsByProvider(providerId: string): Observable<Product[]> {
    // Correct endpoint for owner is /owner/{id}
    return this.http.get<any[]>(`${this.apiUrl}/owner/${providerId}`).pipe(
      map(items => (Array.isArray(items) ? items : []).map(item => this.mapToProduct(item)))
    );
  }

  getProductById(id: string): Observable<Product> {
    return this.http.get<any>(`${this.apiUrl}/${id}`).pipe(
      map(item => this.mapToProduct(item))
    );
  }

  validateProductPayload(product: Partial<Product>): string | null {
    if (!product.name || product.name.trim().length < 2) {
      return 'Product name must be at least 2 characters.';
    }
    if (!product.description || product.description.trim().length < 10) {
      return 'Description must be at least 10 characters.';
    }
    if (!product.categoryId) {
      return 'Please select a category.';
    }
    if (!product.availableForRent && !product.availableForSale) {
      return 'The product must be available for at least rent or sale (or both).';
    }
    if (product.availableForRent && (!product.pricePerDay || product.pricePerDay <= 0)) {
      return 'A daily rental price (> 0) is required when "Available for Rent" is checked.';
    }
    if (product.availableForSale && (!product.purchasePrice || product.purchasePrice <= 0)) {
      return 'A purchase price (> 0) is required when "Available for Sale" is checked.';
    }
    if (product.stock == null || product.stock < 0) {
      return 'Stock must be a non-negative number.';
    }
    return null; // valid
  }

  addProduct(product: Partial<Product>): Observable<Product> {
    const request = {
      name: product.name,
      description: product.description,
      pricePerDay: product.pricePerDay,
      purchasePrice: product.purchasePrice,
      availableForRent: !!product.availableForRent,
      availableForSale: !!product.availableForSale,
      stock: product.stock,
      categoryId: product.categoryId,
      photos: [product.imageUrl],
      condition: product.condition,
      specifications: product.specifications,
      weight: product.weight,
      warehouseId: product.warehouseId || null
    };
    return this.http.post<any>(this.apiUrl, request).pipe(
      map(item => this.mapToProduct(item))
    );
  }

  updateProduct(id: string, product: Partial<Product>): Observable<Product> {
    const request = {
      name: product.name,
      description: product.description,
      pricePerDay: product.pricePerDay,
      purchasePrice: product.purchasePrice,
      availableForRent: !!product.availableForRent,
      availableForSale: !!product.availableForSale,
      stock: product.stock,
      categoryId: product.categoryId,
      photos: [product.imageUrl],
      condition: product.condition,
      specifications: product.specifications,
      weight: product.weight,
      warehouseId: product.warehouseId || null
    };
    return this.http.put<any>(`${this.apiUrl}/${id}`, request).pipe(
      map(item => this.mapToProduct(item))
    );
  }

  deleteProduct(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }

  // Fetch periods when equipment is unavailable (booked or blocked)
  getUnavailablePeriods(equipmentId: string | number): Observable<any[]> {
    return this.http.get<any[]>(
      `${environment.apiUrl}/api/marketplace/equipment/${equipmentId}/unavailable-periods`
    );
  }

  // Gear provider: fetch blocked periods for their equipment
  getBlockedPeriods(equipmentId: string | number): Observable<any[]> {
    return this.http.get<any[]>(
      `${environment.apiUrl}/api/marketplace/equipment/${equipmentId}/blocked-periods`
    );
  }

  // Gear provider: add a blocked period
  addBlockedPeriod(payload: {
    equipmentId: number; startDate: string;
    endDate: string; reason: string;
  }): Observable<any> {
    return this.http.post(
      `${environment.apiUrl}/api/marketplace/equipment/blocked-periods`, payload
    );
  }

  // Gear provider: remove a blocked period
  deleteBlockedPeriod(id: number): Observable<any> {
    return this.http.delete(
      `${environment.apiUrl}/api/marketplace/equipment/blocked-periods/${id}`
    );
  }

  getReviewsByEquipment(equipmentId: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${environment.apiUrl}/api/marketplace/reviews/equipment/${equipmentId}`
    );
  }

  addReview(productId: string, review: { rating: number; comment: string }): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/marketplace/reviews`, {
      equipmentId: parseInt(productId, 10),
      rating: review.rating,
      comment: review.comment
    });
  }

  updateReview(reviewId: number, review: { equipmentId: number; rating: number; comment: string }): Observable<any> {
    return this.http.put(
      `${environment.apiUrl}/api/marketplace/reviews/${reviewId}`,
      review
    );
  }

  private mapToProduct(item: any): Product {
    return {
      id: item?.id ? item.id.toString() : '',
      name: item?.name || '',
      description: item?.description || '',
      price: item?.pricePerDay || item?.purchasePrice || 0,
      pricePerDay: item?.pricePerDay,
      purchasePrice: item?.purchasePrice,
      availableForRent: item?.availableForRent ?? false,
      availableForSale: item?.availableForSale ?? false,
      condition: item?.condition,
      specifications: item?.specifications,
      weight: item?.weight,
      imageUrl: item?.photos && item.photos.length > 0
        ? (() => {
          const url: string = item.photos[0];
          // Unsplash direct photo links need size params to load
          if (url && url.includes('images.unsplash.com') && !url.includes('?')) {
            return url + '?w=600&q=80';
          }
          return url;
        })()
        : 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 300 300"><rect width="300" height="300" fill="%23e8f5e9"/><text x="50%25" y="45%25" dominant-baseline="middle" text-anchor="middle" font-size="64">🏕️</text><text x="50%25" y="68%25" dominant-baseline="middle" text-anchor="middle" font-size="18" fill="%23555">No image</text></svg>',
      category: item?.categoryName || 'General',
      categoryId: item?.categoryId,
      providerId: item?.ownerId ? item.ownerId.toString() : '',
      ownerId: item?.ownerId,
      stock: item?.stock || 0,
      rating: item?.rating || 0,
      reviews: [],
      warehouseId: item?.warehouseId || undefined,
      warehouseName: item?.warehouseName || undefined,
      createdAt: item?.createdAt || undefined
    };
  }
}
