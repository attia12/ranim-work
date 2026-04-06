import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../../services/product.service';
import { Product } from '../../../models/product.model';
import { CartService } from '../../../services/cart.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrl: './shop.component.css'
})
export class ShopComponent implements OnInit {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  categories: string[] = ['All'];
  selectedCategory: string = 'All';
  searchQuery: string = '';
  isLoading = true;
  loadError = '';
  currentPage = 1;

  constructor(
    private productService: ProductService,
    private router: Router,
    private cartService: CartService,
    private notifService: NotificationService
  ) { }

  ngOnInit() {
    this.loadProducts();
  }

  private loadProducts(): void {
    this.isLoading = true;
    this.loadError = '';
    this.productService.getProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.filteredProducts = products;
        const uniqueCats = new Set(products.map(p => p.category).filter(c => c && c !== 'All'));
        this.categories = ['All', ...Array.from(uniqueCats)];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load products in shop:', err);
        this.loadError = 'Failed to load products. Please refresh the page.';
        this.isLoading = false;
      }
    });
  }

  filterProducts() {
    const search = (this.searchQuery || '').toLowerCase();
    this.filteredProducts = this.products.filter(p => {
      const matchesCategory = this.selectedCategory === 'All' || p.category === this.selectedCategory;
      const name = (p.name || '').toLowerCase();
      const desc = (p.description || '').toLowerCase();
      const matchesSearch = name.includes(search) || desc.includes(search);
      return matchesCategory && matchesSearch;
    });
    this.currentPage = 1;
  }

  selectCategory(category: string) {
    this.selectedCategory = category;
    this.filterProducts();
  }

  openDetails(product: Product) {
    this.router.navigate(['/shop/product', product.id]);
  }

  addToCart(product: Product) {
    this.cartService.addToCart(product);
    this.notifService.push({
      type: 'info',
      title: 'Added to Cart',
      message: `${product.name} has been added to your cart.`,
      icon: 'fa-check-circle',
      iconColor: '#10b981'
    });
  }

  isNewProduct(product: Product): boolean {
    if (!product.createdAt) return false;
    const createdDate = new Date(product.createdAt as string | Date);
    const now = new Date();
    const daysDiff = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24));
    return daysDiff <= 14;
  }
}
