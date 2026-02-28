import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface WeatherData {
  temperature: number;
  description: string;
  icon: string;
  location: string;
  humidity: number;
  windSpeed: number;
}

@Injectable({
  providedIn: 'root'
})
export class WeatherService {
  constructor(private http: HttpClient) {}

  getWeather(tourId: number): Observable<WeatherData> {
    return this.http.get<WeatherData>(`${environment.apiUrl}/tours/${tourId}/weather`);
  }
}
