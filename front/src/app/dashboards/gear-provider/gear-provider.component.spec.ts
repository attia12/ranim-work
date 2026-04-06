import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { GearProviderComponent } from './gear-provider.component';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';

describe('GearProviderComponent', () => {
  let component: GearProviderComponent;
  let fixture: ComponentFixture<GearProviderComponent>;

  const mockProductService = {
    getProducts: jasmine.createSpy('getProducts').and.returnValue(of([])),
    getCategories: jasmine.createSpy('getCategories').and.returnValue(of([]))
  };

  const mockAuthService = {
    currentUser$: of(null),
    logout: jasmine.createSpy('logout')
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GearProviderComponent],
      providers: [
        { provide: ProductService, useValue: mockProductService },
        { provide: AuthService, useValue: mockAuthService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(GearProviderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
