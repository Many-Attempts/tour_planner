import { ChangeDetectorRef, Component, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { TourService } from '../../../core/services/tour.service';
import { TransportType } from '../../../core/models';
import { TourMapComponent } from '../../../shared/tour-map.component';
import { LucideAngularModule, ArrowLeft, Save, MapPin } from 'lucide-angular';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [CommonModule, FormsModule, TourMapComponent, LucideAngularModule],
  templateUrl: './tour-form.component.html',
  styleUrl: './tour-form.component.scss'
})
export class TourFormComponent implements OnInit {
  @ViewChild('tourForm') tourForm!: NgForm;

  isEdit = false;
  tourId: number | null = null;
  error = signal('');
  loading = signal(false);

  name = '';
  description = '';
  from = '';
  to = '';
  transportType: TransportType = 'HIKING';
  tourDistance: number | null = null;
  estimatedTime: number | null = null;
  routeGeoJson: string | null = null;

  transportTypes: { value: TransportType; label: string }[] = [
    { value: 'BICYCLE', label: 'Bike' },
    { value: 'HIKING', label: 'Hiking' },
    { value: 'RUNNING', label: 'Running' },
    { value: 'CAR', label: 'Vacation' }
  ];

  readonly ArrowLeftIcon = ArrowLeft;
  readonly SaveIcon = Save;
  readonly MapPinIcon = MapPin;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tourService: TourService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.tourId = Number(id);
      this.tourService.getTourById(this.tourId).subscribe(tour => {
        this.name = tour.name;
        this.description = tour.description;
        this.from = tour.from;
        this.to = tour.to;
        this.transportType = tour.transportType;
        this.tourDistance = tour.tourDistance;
        this.estimatedTime = tour.estimatedTime;
        this.routeGeoJson = tour.routeInformation;
        this.cdr.markForCheck();
      });
    }
  }

  selectTransport(type: TransportType): void {
    this.transportType = type;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  onSubmit(): void {
    if (this.tourForm.invalid) {
      Object.values(this.tourForm.controls).forEach(c => c.markAsTouched());
      this.error.set('Please fill in all required fields.');
      return;
    }

    this.error.set('');
    this.loading.set(true);

    const request = {
      name: this.name,
      description: this.description,
      from: this.from,
      to: this.to,
      transportType: this.transportType,
      tourDistance: this.tourDistance ?? undefined,
      estimatedTime: this.estimatedTime ?? undefined
    };

    const source$ = this.isEdit && this.tourId
      ? this.tourService.updateTour(this.tourId, request)
      : this.tourService.createTour(request);

    source$.pipe(
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
