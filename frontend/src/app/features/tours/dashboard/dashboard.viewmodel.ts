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

  readonly error = signal('');

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

  // downloads all tours as a json file
  onExport(): void {
    this.error.set('');
    this.tourService.exportTours().subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'tours-export.json';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      },
      error: err => this.error.set('Export failed: ' + (err?.error?.message ?? err.message))
    });
  }

  // reads the json file and sends the tours to the backend
  onImport(file: File | null | undefined): void {
    this.error.set('');
    if (!file) return;
    file.text()
      .then(text => {
        let payload: unknown;
        try {
          payload = JSON.parse(text);
        } catch {
          this.error.set('Invalid JSON file');
          return;
        }
        if (!Array.isArray(payload)) {
          this.error.set('Expected a JSON array of tours');
          return;
        }
        this.tourService.importTours(payload).subscribe({
          error: err => this.error.set('Import failed: ' + (err?.error?.message ?? err.message))
        });
      })
      .catch(err => this.error.set('Could not read file: ' + err.message));
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
