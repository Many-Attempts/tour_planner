import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TourService } from '../../../core/services/tour.service';
import { TransportType } from '../../../core/models/tour.model';
import { TourMapComponent } from '../../../shared/components/tour-map.component';
import { LucideAngularModule, ArrowLeft, Save, MapPin } from 'lucide-angular';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [CommonModule, FormsModule, TourMapComponent, LucideAngularModule],
  templateUrl: './tour-form.component.html',
  styleUrl: './tour-form.component.scss'
})
export class TourFormComponent implements OnInit {
  isEdit = false;
  tourId: number | null = null;

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
    const request = {
      name: this.name,
      description: this.description,
      from: this.from,
      to: this.to,
      transportType: this.transportType,
      tourDistance: this.tourDistance ?? undefined,
      estimatedTime: this.estimatedTime ?? undefined
    };

    if (this.isEdit && this.tourId) {
      this.tourService.updateTour(this.tourId, request).subscribe(tour => {
        this.router.navigate(['/tour', tour.id]);
      });
    } else {
      this.tourService.createTour(request).subscribe(tour => {
        this.router.navigate(['/tour', tour.id]);
      });
    }
  }

}
