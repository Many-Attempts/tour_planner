import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { Tour } from '../../../core/models';
import { TourService } from '../../../core/services/tour.service';
import {
  formatDistance,
  formatTime,
  getTransportLabel
} from '../../../shared/format.utils';

@Injectable()
export class DashboardViewModel {
  private tourService = inject(TourService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  readonly tours = toSignal(this.tourService.tours$, { initialValue: [] as Tour[] });

  readonly searchQuery = signal('');

  private searchSubject = new Subject<string>();

  init(): void {
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(query => {
        this.tourService.loadTours(query).subscribe({
          error: err => console.error('Search failed', err)
        });
      });

    this.tourService.loadTours().subscribe({
      error: err => console.error('Failed to load tours', err)
    });
  }

  onSearch(value: string): void {
    this.searchQuery.set(value);
    this.searchSubject.next(value);
  }

  navigateToTour(id: number): void {
    this.router.navigate(['/tour', id]);
  }

  navigateToCreate(): void {
    this.router.navigate(['/create-tour']);
  }

  formatTime(seconds: number | null): string {
    return formatTime(seconds);
  }

  formatDistance(km: number | null): string {
    return formatDistance(km);
  }

  getTransportLabel(type: string): string {
    return getTransportLabel(type);
  }
}
