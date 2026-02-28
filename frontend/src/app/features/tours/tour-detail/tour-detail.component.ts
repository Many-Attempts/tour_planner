import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TourService } from '../../../core/services/tour.service';
import { TourLogService } from '../../../core/services/tour-log.service';
import { Tour, TourLog, Difficulty } from '../../../core/models/tour.model';
import { TourMapComponent } from '../../../shared/components/tour-map.component';
import {
  LucideAngularModule, ArrowLeft, Edit, Trash2, MapPin, Clock,
  Calendar, Plus, Star, TrendingUp, Award
} from 'lucide-angular';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, TourMapComponent, LucideAngularModule],
  templateUrl: './tour-detail.component.html',
  styleUrl: './tour-detail.component.scss'
})
export class TourDetailComponent implements OnInit {
  tour: Tour | null = null;
  logs: TourLog[] = [];
  showAddLog = false;
  editingLogId: number | null = null;

  // New log form
  newLog = {
    dateTime: '',
    comment: '',
    difficulty: 'MEDIUM' as Difficulty,
    totalDistance: 0,
    totalTime: 0,
    rating: 0
  };

  readonly ArrowLeftIcon = ArrowLeft;
  readonly EditIcon = Edit;
  readonly Trash2Icon = Trash2;
  readonly MapPinIcon = MapPin;
  readonly ClockIcon = Clock;
  readonly CalendarIcon = Calendar;
  readonly PlusIcon = Plus;
  readonly StarIcon = Star;
  readonly TrendingUpIcon = TrendingUp;
  readonly AwardIcon = Award;

  difficulties: Difficulty[] = ['EASY', 'MEDIUM', 'HARD', 'EXPERT'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tourService: TourService,
    private tourLogService: TourLogService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadTour(id);
    this.loadLogs(id);
  }

  loadTour(id: number): void {
    this.tourService.getTourById(id).subscribe({
      next: tour => this.tour = tour,
      error: err => {
        console.error('Failed to load tour', err);
        this.router.navigate(['/dashboard']);
      }
    });
  }

  loadLogs(id: number): void {
    this.tourLogService.getLogs(id).subscribe({
      next: logs => this.logs = logs,
      error: err => console.error('Failed to load logs', err)
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  editTour(): void {
    if (this.tour) {
      this.router.navigate(['/edit-tour', this.tour.id]);
    }
  }

  deleteTour(): void {
    if (this.tour && confirm('Are you sure you want to delete this tour?')) {
      this.tourService.deleteTour(this.tour.id).subscribe(() => {
        this.router.navigate(['/dashboard']);
      });
    }
  }

  toggleAddLog(): void {
    this.showAddLog = !this.showAddLog;
    this.editingLogId = null;
    this.resetLogForm();
  }

  setRating(stars: number): void {
    this.newLog.rating = stars;
  }

  saveLog(): void {
    if (!this.tour) return;

    const request = {
      dateTime: this.newLog.dateTime || new Date().toISOString(),
      comment: this.newLog.comment,
      difficulty: this.newLog.difficulty,
      totalDistance: this.newLog.totalDistance,
      totalTime: this.newLog.totalTime,
      rating: this.newLog.rating || 1
    };

    if (this.editingLogId) {
      this.tourLogService.updateLog(this.tour.id, this.editingLogId, request).subscribe(() => {
        this.loadLogs(this.tour!.id);
        this.loadTour(this.tour!.id);
        this.showAddLog = false;
        this.editingLogId = null;
        this.resetLogForm();
      });
    } else {
      this.tourLogService.createLog(this.tour.id, request).subscribe(() => {
        this.loadLogs(this.tour!.id);
        this.loadTour(this.tour!.id);
        this.showAddLog = false;
        this.resetLogForm();
      });
    }
  }

  editLog(log: TourLog): void {
    this.editingLogId = log.id;
    this.showAddLog = true;
    this.newLog = {
      dateTime: log.dateTime,
      comment: log.comment,
      difficulty: log.difficulty,
      totalDistance: log.totalDistance,
      totalTime: log.totalTime,
      rating: log.rating
    };
  }

  deleteLog(log: TourLog): void {
    if (!this.tour || !confirm('Delete this log?')) return;
    this.tourLogService.deleteLog(this.tour.id, log.id).subscribe(() => {
      this.loadLogs(this.tour!.id);
      this.loadTour(this.tour!.id);
    });
  }

  cancelLog(): void {
    this.showAddLog = false;
    this.editingLogId = null;
    this.resetLogForm();
  }

  private resetLogForm(): void {
    this.newLog = {
      dateTime: '',
      comment: '',
      difficulty: 'MEDIUM',
      totalDistance: 0,
      totalTime: 0,
      rating: 0
    };
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

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' });
  }

  formatTimeFromDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
  }

  getTransportLabel(type: string): string {
    const labels: Record<string, string> = {
      CAR: 'Car', BICYCLE: 'Bike', WALKING: 'Walking', RUNNING: 'Running', HIKING: 'Hiking'
    };
    return labels[type] || type;
  }

  getStarArray(count: number): number[] {
    return Array.from({ length: count }, (_, i) => i + 1);
  }
}
