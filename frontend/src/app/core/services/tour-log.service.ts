import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TourLog, TourLogRequest } from '../models/tour.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TourLogService {
  constructor(private http: HttpClient) {}

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
}
