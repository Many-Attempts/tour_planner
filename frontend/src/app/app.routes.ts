import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/tours/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'tour/:id',
    loadComponent: () => import('./features/tours/tour-detail/tour-detail.component').then(m => m.TourDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'create-tour',
    loadComponent: () => import('./features/tours/tour-form/tour-form.component').then(m => m.TourFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'edit-tour/:id',
    loadComponent: () => import('./features/tours/tour-form/tour-form.component').then(m => m.TourFormComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
