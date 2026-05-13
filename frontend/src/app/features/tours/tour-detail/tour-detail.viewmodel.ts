import { Injectable, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Difficulty, Tour, TourLog, WeatherData } from '../../../core/models';
import { TourService } from '../../../core/services/tour.service';
import {
  formatDate,
  formatDistance,
  formatTime,
  formatTimeFromDate,
  getStarArray,
  getTransportLabel
} from '../../../shared/format.utils';

@Injectable()
export class TourDetailViewModel {
  private tourService = inject(TourService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly tour = signal<Tour | null>(null);
  readonly logs = signal<TourLog[]>([]);
  readonly weather = signal<WeatherData | null>(null);
  readonly weatherUnavailable = signal(false);

  readonly showAddLog = signal(false);
  readonly editingLogId = signal<number | null>(null);
  readonly logDateTime = signal('');
  readonly logComment = signal('');
  readonly logDifficulty = signal<Difficulty>('MEDIUM');
  readonly logTotalDistance = signal(0);
  readonly logTotalTime = signal(0);
  readonly logRating = signal(0);

  readonly difficulties: Difficulty[] = ['EASY', 'MEDIUM', 'HARD', 'EXPERT'];

  init(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadTour(id);
    this.loadLogs(id);
    this.loadWeather(id);
  }

  // backend sends 404 when there is no weather data for the location
  loadWeather(id: number): void {
    this.weather.set(null);
    this.weatherUnavailable.set(false);
    this.tourService.getWeather(id).subscribe({
      next: data => {
        this.weather.set(data);
        this.weatherUnavailable.set(false);
      },
      error: () => {
        this.weather.set(null);
        this.weatherUnavailable.set(true);
      }
    });
  }

  weatherIconUrl(icon: string): string {
    return `https://openweathermap.org/img/wn/${icon}@2x.png`;
  }

  loadTour(id: number): void {
    this.tourService.getTourById(id).subscribe({
      next: tour => this.tour.set(tour),
      error: err => {
        console.error('Failed to load tour', err);
        this.router.navigate(['/dashboard']);
      }
    });
  }

  loadLogs(id: number): void {
    this.tourService.getLogs(id).subscribe({
      next: logs => this.logs.set(logs),
      error: err => console.error('Failed to load logs', err)
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  editTour(): void {
    const tour = this.tour();
    if (tour) {
      this.router.navigate(['/edit-tour', tour.id]);
    }
  }

  deleteTour(): void {
    const tour = this.tour();
    if (tour && confirm('Are you sure you want to delete this tour?')) {
      this.tourService.deleteTour(tour.id).subscribe(() => {
        this.router.navigate(['/dashboard']);
      });
    }
  }

  toggleAddLog(): void {
    this.showAddLog.update(v => !v);
    this.editingLogId.set(null);
    this.resetLogForm();
  }

  setRating(stars: number): void {
    this.logRating.set(stars);
  }

  saveLog(): void {
    const tour = this.tour();
    if (!tour) return;

    const request = {
      dateTime: this.logDateTime() || new Date().toISOString(),
      comment: this.logComment(),
      difficulty: this.logDifficulty(),
      totalDistance: this.logTotalDistance(),
      totalTime: this.logTotalTime(),
      rating: this.logRating() || 1
    };

    const editingId = this.editingLogId();
    const op$ = editingId
      ? this.tourService.updateLog(tour.id, editingId, request)
      : this.tourService.createLog(tour.id, request);

    op$.subscribe(() => {
      this.loadLogs(tour.id);
      this.loadTour(tour.id);
      this.showAddLog.set(false);
      this.editingLogId.set(null);
      this.resetLogForm();
    });
  }

  editLog(log: TourLog): void {
    this.editingLogId.set(log.id);
    this.showAddLog.set(true);
    this.logDateTime.set(log.dateTime);
    this.logComment.set(log.comment);
    this.logDifficulty.set(log.difficulty);
    this.logTotalDistance.set(log.totalDistance);
    this.logTotalTime.set(log.totalTime);
    this.logRating.set(log.rating);
  }

  deleteLog(log: TourLog): void {
    const tour = this.tour();
    if (!tour || !confirm('Delete this log?')) return;
    this.tourService.deleteLog(tour.id, log.id).subscribe(() => {
      this.loadLogs(tour.id);
      this.loadTour(tour.id);
    });
  }

  cancelLog(): void {
    this.showAddLog.set(false);
    this.editingLogId.set(null);
    this.resetLogForm();
  }

  private resetLogForm(): void {
    this.logDateTime.set('');
    this.logComment.set('');
    this.logDifficulty.set('MEDIUM');
    this.logTotalDistance.set(0);
    this.logTotalTime.set(0);
    this.logRating.set(0);
  }

  formatTime(seconds: number | null): string {
    return formatTime(seconds);
  }

  formatDistance(km: number | null): string {
    return formatDistance(km);
  }

  formatDate(dateStr: string): string {
    return formatDate(dateStr);
  }

  formatTimeFromDate(dateStr: string): string {
    return formatTimeFromDate(dateStr);
  }

  getTransportLabel(type: string): string {
    return getTransportLabel(type);
  }

  getStarArray(count: number): number[] {
    return getStarArray(count);
  }
}
