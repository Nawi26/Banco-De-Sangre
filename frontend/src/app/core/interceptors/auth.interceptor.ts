import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { API_BASE } from '../config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const esLlamadaAlBackend = req.url.startsWith(API_BASE);
  const token = authService.getToken();

  const solicitud = (esLlamadaAlBackend && token)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(solicitud).pipe(
    catchError(error => {
      if (esLlamadaAlBackend && error.status === 401) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
