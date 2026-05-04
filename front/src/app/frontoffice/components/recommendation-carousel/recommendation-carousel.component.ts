import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectionStrategy,
  ChangeDetectorRef
} from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { RecommendationService } from '../../../services/recommendation.service';
import { RecommendedCampsite } from '../../../models/recommended-campsite.model';

@Component({
  selector: 'app-recommendation-carousel',
  templateUrl: './recommendation-carousel.component.html',
  styleUrls: ['./recommendation-carousel.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RecommendationCarouselComponent implements OnInit, OnDestroy {

  recommendations: RecommendedCampsite[] = [];
  loading = true;
  error = false;
  activeIndex = 0;

  private autoAdvanceInterval: ReturnType<typeof setInterval> | null = null;
  private sub: Subscription | null = null;

  constructor(
    private recommendationService: RecommendationService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.sub = this.recommendationService.getRecommendations().subscribe({
      next: (data) => {
        this.recommendations = data;
        this.loading = false;
        if (data.length === 0) {
          this.error = true;
        } else {
          this.startAutoAdvance();
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.error = true;
        this.cdr.markForCheck();
      },
      complete: () => {
        // EMPTY stream completes without error → show fallback
        if (this.loading) {
          this.loading = false;
          this.error = true;
          this.cdr.markForCheck();
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.stopAutoAdvance();
    this.sub?.unsubscribe();
  }

  prev(): void {
    this.activeIndex =
      this.activeIndex === 0
        ? this.recommendations.length - 1
        : this.activeIndex - 1;
    this.cdr.markForCheck();
  }

  next(): void {
    this.activeIndex =
      this.activeIndex === this.recommendations.length - 1
        ? 0
        : this.activeIndex + 1;
    this.cdr.markForCheck();
  }

  goTo(index: number): void {
    this.activeIndex = index;
    this.cdr.markForCheck();
  }

  navigate(campsiteId: number): void {
    this.router.navigate(['/campsites', campsiteId]);
  }

  scoreColor(score: number): string {
    if (score >= 70) return '#16a34a'; // green
    if (score >= 45) return '#d97706'; // amber
    return '#dc2626';                  // red
  }

  svgCircle(score: number): string {
    const r = 20;
    const circumference = 2 * Math.PI * r;
    const offset = circumference - (score / 100) * circumference;
    return offset.toFixed(2);
  }

  featureIcon(feature: string): string {
    const map: Record<string, string> = {
      FOREST: '🌲', LAKE: '🏞️', MOUNTAIN: '⛰️',
      BEACH: '🏖️', RIVER: '🏞️', PLAIN: '🌾'
    };
    return map[feature?.toUpperCase()] ?? '🏕️';
  }

  weatherIcon(description: string): string {
    if (!description) return '☀️';
    const d = description.toLowerCase();
    if (d.includes('orage')) return '⛈️';
    if (d.includes('neige')) return '❄️';
    if (d.includes('pluie') || d.includes('bruine') || d.includes('averse')) return '🌧️';
    if (d.includes('brouillard')) return '🌫️';
    if (d.includes('nuageux') || d.includes('couvert')) return '🌤️';
    return '☀️';
  }

  get skeletons(): number[] {
    return [0, 1, 2, 3, 4];
  }

  private startAutoAdvance(): void {
    this.autoAdvanceInterval = setInterval(() => {
      this.next();
    }, 5000);
  }

  private stopAutoAdvance(): void {
    if (this.autoAdvanceInterval !== null) {
      clearInterval(this.autoAdvanceInterval);
      this.autoAdvanceInterval = null;
    }
  }
}
