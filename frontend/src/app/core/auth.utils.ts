import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  router.navigate(['/']);
  return false;
};

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // grab the router here, inject() inside catchError would crash because the injection context is gone
  const router = inject(Router);
  const token = localStorage.getItem('token');
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError(err => {
      if ((err.status === 401 || err.status === 403) && !req.url.includes('/api/auth/')) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.navigate(['/']);
      }
      return throwError(() => err);
    })
  );
};
