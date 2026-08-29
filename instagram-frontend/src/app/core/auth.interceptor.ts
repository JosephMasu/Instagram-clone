import {
  HttpBackend,
  HttpClient,
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, shareReplay, switchMap, throwError } from 'rxjs';
import { AuthStore } from './auth.store';
import { AuthResponse } from '../shared/models/auth/model';

let refreshInFlight$: Observable<AuthResponse> | null = null;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(AuthStore);
  const router = inject(Router);
  const refreshClient = new HttpClient(inject(HttpBackend));
  const token = store.currentAccessToken();

  const skipAuth =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/register') ||
    req.url.includes('/auth/refresh');

  const authorized =
    !skipAuth && token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authorized).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || skipAuth || !store.refreshToken()) {
        return throwError(() => error);
      }

      if (!refreshInFlight$) {
        refreshInFlight$ = refreshClient
          .post<AuthResponse>('/api/v1/auth/refresh', {
            refreshToken: store.refreshToken(),
          })
          .pipe(
            finalize(() => {
              refreshInFlight$ = null;
            }),
            shareReplay(1),
          );
      }

      return refreshInFlight$.pipe(
        switchMap((tokens) => {
          store.setTokens(tokens.accessToken, tokens.refreshToken);
          return next(
            req.clone({
              setHeaders: { Authorization: `Bearer ${tokens.accessToken}` },
            }),
          );
        }),
        catchError((refreshError) => {
          store.logout();
          void router.navigateByUrl('/login');
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
