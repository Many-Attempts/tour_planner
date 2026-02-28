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
