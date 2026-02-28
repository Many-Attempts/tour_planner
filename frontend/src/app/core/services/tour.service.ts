import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Tour, TourRequest } from '../models/tour.model';
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

  private refreshTours(): void {
    this.http.get<Tour[]>(`${environment.apiUrl}/tours`).subscribe({
      next: tours => this.toursSubject.next(tours),
      error: err => console.error('Failed to refresh tours', err)
    });
  }
}
