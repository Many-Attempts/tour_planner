import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TourService } from '../../../core/services/tour.service';
import { Tour } from '../../../core/models/tour.model';
import { NavbarComponent } from '../../../shared/components/navbar.component';
import { LucideAngularModule, Search, Plus, Upload, Download, MapPin, Clock } from 'lucide-angular';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, LucideAngularModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  tours: Tour[] = [];
  searchQuery = '';
  private searchSubject = new Subject<string>();

  readonly SearchIcon = Search;
  readonly PlusIcon = Plus;
  readonly UploadIcon = Upload;
  readonly DownloadIcon = Download;
  readonly MapPinIcon = MapPin;
  readonly ClockIcon = Clock;

  constructor(private tourService: TourService, private router: Router, private cdr: ChangeDetectorRef) {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => {
      this.tourService.loadTours(query).subscribe({
        error: err => console.error('Search failed', err)
      });
    });
  }

  ngOnInit(): void {
    // Subscribe to the tours stream for reactive updates
    this.tourService.tours$.subscribe(tours => {
      this.tours = tours;
      this.cdr.markForCheck();
    });
    // Trigger initial load
    this.tourService.loadTours().subscribe({
      error: err => console.error('Failed to load tours', err)
    });
  }

  onSearch(): void {
    this.searchSubject.next(this.searchQuery);
  }

  navigateToTour(id: number): void {
    this.router.navigate(['/tour', id]);
  }

  navigateToCreate(): void {
    this.router.navigate(['/create-tour']);
  }

  formatTime(seconds: number | null): string {
    if (!seconds) return '-';
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    if (hours > 0) return `${hours}h ${minutes}m`;
    return `${minutes}m`;
  }

  formatDistance(km: number | null): string {
    if (!km) return '-';
    return `${km.toFixed(1)} km`;
  }

  getTransportLabel(type: string): string {
    const labels: Record<string, string> = {
      CAR: 'Car',
      BICYCLE: 'Bike',
      WALKING: 'Walking',
      RUNNING: 'Running',
      HIKING: 'Hiking'
    };
    return labels[type] || type;
  }
}
