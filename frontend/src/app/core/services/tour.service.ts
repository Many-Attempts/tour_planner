import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Tour, TourRequest, TourLog, TourLogRequest, WeatherData } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TourService {
  private toursSubject = new BehaviorSubject<Tour[]>([]);
  public tours$ = this.toursSubject.asObservable();

  constructor(private http: HttpClient) {}

  loadTours(search?: string): Observable<Tour[]> {
    let params = new HttpParams();
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<Tour[]>(`${environment.apiUrl}/tours`, { params }).pipe(
      tap(tours => this.toursSubject.next(tours))
    );
  }

  getTourById(id: number): Observable<Tour> {
    return this.http.get<Tour>(`${environment.apiUrl}/tours/${id}`);
  }

  createTour(request: TourRequest): Observable<Tour> {
    return this.http.post<Tour>(`${environment.apiUrl}/tours`, request).pipe(
      tap(() => this.refreshTours())
    );
  }

  updateTour(id: number, request: TourRequest): Observable<Tour> {
    return this.http.put<Tour>(`${environment.apiUrl}/tours/${id}`, request).pipe(
      tap(() => this.refreshTours())
    );
  }

  deleteTour(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/tours/${id}`).pipe(
      tap(() => this.refreshTours())
    );
  }

  uploadTourImage(id: number, file: File): Observable<Tour> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Tour>(`${environment.apiUrl}/tours/${id}/image`, formData).pipe(
      tap(() => this.refreshTours())
    );
  }

  private refreshTours(): void {
    this.http.get<Tour[]>(`${environment.apiUrl}/tours`).subscribe({
      next: tours => this.toursSubject.next(tours),
      error: err => console.error('Failed to refresh tours', err)
    });
  }

  getLogs(tourId: number): Observable<TourLog[]> {
    return this.http.get<TourLog[]>(`${environment.apiUrl}/tours/${tourId}/logs`);
  }

  createLog(tourId: number, request: TourLogRequest): Observable<TourLog> {
    return this.http.post<TourLog>(`${environment.apiUrl}/tours/${tourId}/logs`, request);
  }

  updateLog(tourId: number, logId: number, request: TourLogRequest): Observable<TourLog> {
    return this.http.put<TourLog>(`${environment.apiUrl}/tours/${tourId}/logs/${logId}`, request);
  }

  deleteLog(tourId: number, logId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/tours/${tourId}/logs/${logId}`);
  }

  getWeather(tourId: number): Observable<WeatherData> {
    return this.http.get<WeatherData>(`${environment.apiUrl}/tours/${tourId}/weather`);
  }
}
