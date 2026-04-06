import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { Product } from '../models/product.model';

const validProduct: Partial<Product> = {
  name: 'Tent Pro',
  description: 'A very good tent for camping',
  categoryId: 1,
  availableForRent: true,
  availableForSale: false,
  pricePerDay: 15,
  stock: 10
};

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductService]
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ── validateProductPayload ─────────────────────────────────────────────────

  it('should return null for a valid product', () => {
    expect(service.validateProductPayload(validProduct)).toBeNull();
  });

  it('should return error when name is missing', () => {
    const result = service.validateProductPayload({ ...validProduct, name: '' });
    expect(result).toBe('Product name must be at least 2 characters.');
  });

  it('should return error when name is too short', () => {
    const result = service.validateProductPayload({ ...validProduct, name: 'A' });
    expect(result).toBe('Product name must be at least 2 characters.');
  });

  it('should return error when description is too short', () => {
    const result = service.validateProductPayload({ ...validProduct, description: 'Short' });
    expect(result).toBe('Description must be at least 10 characters.');
  });

  it('should return error when no category is selected', () => {
    const result = service.validateProductPayload({ ...validProduct, categoryId: undefined });
    expect(result).toBe('Please select a category.');
  });

  it('should return error when neither rent nor sale is selected', () => {
    const result = service.validateProductPayload({
      ...validProduct,
      availableForRent: false,
      availableForSale: false
    });
    expect(result).toBe('The product must be available for at least rent or sale (or both).');
  });

  it('should return error when rent is true but pricePerDay is 0', () => {
    const result = service.validateProductPayload({
      ...validProduct,
      availableForRent: true,
      pricePerDay: 0
    });
    expect(result).toBe('A daily rental price (> 0) is required when "Available for Rent" is checked.');
  });

  it('should return error when rent is true but pricePerDay is missing', () => {
    const result = service.validateProductPayload({
      ...validProduct,
      availableForRent: true,
      pricePerDay: undefined
    });
    expect(result).toBe('A daily rental price (> 0) is required when "Available for Rent" is checked.');
  });

  it('should return error when sale is true but purchasePrice is 0', () => {
    const result = service.validateProductPayload({
      ...validProduct,
      availableForRent: false,
      availableForSale: true,
      purchasePrice: 0
    });
    expect(result).toBe('A purchase price (> 0) is required when "Available for Sale" is checked.');
  });

  it('should return error when stock is negative', () => {
    const result = service.validateProductPayload({ ...validProduct, stock: -1 });
    expect(result).toBe('Stock must be a non-negative number.');
  });

  it('should return null when stock is 0', () => {
    expect(service.validateProductPayload({ ...validProduct, stock: 0 })).toBeNull();
  });

  it('should be valid when both rent and sale are enabled with correct prices', () => {
    const result = service.validateProductPayload({
      ...validProduct,
      availableForRent: true,
      availableForSale: true,
      pricePerDay: 10,
      purchasePrice: 100
    });
    expect(result).toBeNull();
  });

  // ── HTTP methods ───────────────────────────────────────────────────────────

  it('should call GET /equipment for getProducts()', () => {
    service.getProducts().subscribe();
    const req = httpMock.expectOne('http://localhost:9099/api/marketplace/equipment');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should call GET /categories for getCategories()', () => {
    service.getCategories().subscribe();
    const req = httpMock.expectOne('http://localhost:9099/api/marketplace/categories');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should call DELETE for deleteProduct()', () => {
    service.deleteProduct('5').subscribe();
    const req = httpMock.expectOne('http://localhost:9099/api/marketplace/equipment/5');
    expect(req.request.method).toBe('DELETE');
    req.flush('deleted');
  });
});
