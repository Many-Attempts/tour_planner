export type TransportType = 'CAR' | 'BICYCLE' | 'WALKING' | 'RUNNING' | 'HIKING';
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD' | 'EXPERT';

export interface Tour {
  id: number;
  name: string;
  description: string;
  from: string;
  to: string;
  transportType: TransportType;
  tourDistance: number;
  estimatedTime: number;
  routeInformation: string | null;
  logCount: number;
  popularity: string;
  childFriendliness: string;
  createdAt: string;
  updatedAt: string;
}

export interface TourRequest {
  name: string;
  description: string;
  from: string;
  to: string;
  transportType: TransportType;
  tourDistance?: number;
  estimatedTime?: number;
}

export interface TourLog {
  id: number;
  dateTime: string;
  comment: string;
  difficulty: Difficulty;
  totalDistance: number;
  totalTime: number;
  rating: number;
  tourId: number;
}

export interface TourLogRequest {
  dateTime: string;
  comment: string;
  difficulty: Difficulty;
  totalDistance: number;
  totalTime: number;
  rating: number;
}

export interface User {
  email: string;
  username: string;
  token: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  username: string;
}

export interface WeatherData {
  temperature: number;
  description: string;
  icon: string;
  location: string;
  humidity: number;
  windSpeed: number;
}
