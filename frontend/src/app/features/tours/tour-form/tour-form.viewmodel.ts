import { Injectable, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, switchMap } from 'rxjs';
import { Tour, TransportType } from '../../../core/models';
import { TourService } from '../../../core/services/tour.service';

// signals so the view updates without zone.js (zoneless change detection)
@Injectable()
export class TourFormViewModel {
  private tourService = inject(TourService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly isEdit = signal(false);
  private tourId: number | null = null;

  readonly name = signal('');
  readonly description = signal('');
  readonly from = signal('');
  readonly to = signal('');
  readonly transportType = signal<TransportType>('HIKING');
  readonly tourDistance = signal<number | null>(null);
  readonly estimatedTime = signal<number | null>(null);
  readonly routeGeoJson = signal<string | null>(null);

  readonly selectedImage = signal<File | null>(null);
  readonly imagePreviewUrl = signal<string | null>(null);

  readonly error = signal('');
  readonly loading = signal(false);

  readonly transportTypes: { value: TransportType; label: string }[] = [
    { value: 'BICYCLE', label: 'Bike' },
    { value: 'HIKING', label: 'Hiking' },
    { value: 'RUNNING', label: 'Running' },
    { value: 'CAR', label: 'Vacation' }
  ];

  init(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.tourId = Number(id);
      this.tourService.getTourById(this.tourId).subscribe(tour => {
        this.name.set(tour.name);
        this.description.set(tour.description);
        this.from.set(tour.from);
        this.to.set(tour.to);
        this.transportType.set(tour.transportType);
        this.tourDistance.set(tour.tourDistance);
        this.estimatedTime.set(tour.estimatedTime);
        this.routeGeoJson.set(tour.routeInformation);
      });
    }
  }

  selectTransport(type: TransportType): void {
    this.transportType.set(type);
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    const previous = this.imagePreviewUrl();
    if (previous) {
      URL.revokeObjectURL(previous);
    }
    this.selectedImage.set(file);
    this.imagePreviewUrl.set(file ? URL.createObjectURL(file) : null);
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  submit(formValid: boolean): void {
    if (!formValid) {
      this.error.set('Please fill in all required fields.');
      return;
    }

    this.error.set('');
    this.loading.set(true);

    const request = {
      name: this.name(),
      description: this.description(),
      from: this.from(),
      to: this.to(),
      transportType: this.transportType(),
      tourDistance: this.tourDistance() ?? undefined,
      estimatedTime: this.estimatedTime() ?? undefined
    };

    const save$ = this.isEdit() && this.tourId
      ? this.tourService.updateTour(this.tourId, request)
      : this.tourService.createTour(request);

    const image = this.selectedImage();
    const withImage$ = image
      ? save$.pipe(switchMap((tour: Tour) => this.tourService.uploadTourImage(tour.id, image)))
      : save$;

    withImage$.pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: tour => {
        this.router.navigate(['/tour', tour.id]);
      },
      error: err => {
        this.error.set(err.error?.message || err.message || 'Failed to save tour. Please try again.');
      }
    });
  }
}
