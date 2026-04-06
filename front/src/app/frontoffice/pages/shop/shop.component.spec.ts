import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ShopComponent } from './shop.component';
import { ProductService } from '../../../services/product.service';
import { of } from 'rxjs';

describe('ShopComponent', () => {
  let component: ShopComponent;
  let fixture: ComponentFixture<ShopComponent>;

  const mockProductService = {
    getProducts: jasmine.createSpy('getProducts').and.returnValue(of([])),
    getCategories: jasmine.createSpy('getCategories').and.returnValue(of([]))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FormsModule  // ✅ nécessaire pour ngModel
      ],
      declarations: [ShopComponent],
      providers: [
        { provide: ProductService, useValue: mockProductService } // ✅ mock sans HttpClient
      ],
      schemas: [NO_ERRORS_SCHEMA] // ✅ ignore les composants enfants inconnus
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShopComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});