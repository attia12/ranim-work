import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ProductService } from '../../../services/product.service';
import { CampsiteService } from '../../../services/campsite.service';
import { Product } from '../../../models/product.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  topEquipment: Product[] = [];
  featuredCampsites: any[] = [];
  isLoadingEquipment = true;
  isLoadingCampsites = true;
  equipmentError = '';
  campsitesError = '';

  constructor(
    private productService: ProductService,
    private campsiteService: CampsiteService
  ) {}

  ngOnInit(): void {
    this.loadTopEquipment();
    this.loadFeaturedCampsites();
  }

  private loadTopEquipment(): void {
    this.productService.getProducts().subscribe({
      next: (products) => {
        this.topEquipment = products
          .filter(p => p.rating && p.rating >= 4)
          .sort((a, b) => (b.rating || 0) - (a.rating || 0))
          .slice(0, 6);
        this.isLoadingEquipment = false;
      },
      error: (err) => {
        console.error('Failed to load equipment:', err);
        this.equipmentError = 'Could not load equipment. Please try again later.';
        this.isLoadingEquipment = false;
      }
    });
  }

  private loadFeaturedCampsites(): void {
    this.campsiteService.search({ page: 0, size: 6 }).subscribe({
      next: (page: any) => {
        this.featuredCampsites = (page.content ?? page)
          .slice(0, 3);
        this.isLoadingCampsites = false;
      },
      error: (err: any) => {
        console.error('Failed to load campsites:', err);
        this.campsitesError = 'Could not load campsites. Please try again later.';
        this.isLoadingCampsites = false;
      }
    });
  }
}
