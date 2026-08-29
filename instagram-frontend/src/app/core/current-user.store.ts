import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, finalize, of, retry, shareReplay, switchMap, tap, throwError, timer } from 'rxjs';
import { AuthApi } from '../services/auth-api';
import { UserApi } from '../services/user-api';
import { UserProfile } from '../shared/models/user/model';
import { UserLookup } from '../services/user-lookup';
import { AuthStore } from './auth.store';

@Injectable({ providedIn: 'root' })
export class CurrentUserStore {
  private readonly users = inject(UserApi);
  private readonly authApi = inject(AuthApi);
  private readonly auth = inject(AuthStore);
  private readonly lookup = inject(UserLookup);

  readonly profile = signal<UserProfile | null>(null);
  private inflight$: Observable<UserProfile> | null = null;

  constructor() {
    this.auth.onLogout(() => {
      this.inflight$ = null;
      this.profile.set(null);
    });
  }

  set(profile: UserProfile | null): void {
    this.profile.set(profile);
  }

  clear(): void {
    this.inflight$ = null;
    this.profile.set(null);
  }

  ensureProfile(): Observable<UserProfile> {
    const cached = this.profile();
    if (cached) {
      return of(cached);
    }
    if (!this.inflight$) {
      this.inflight$ = this.users.me().pipe(
        retry({
          count: 1,
          delay: (error) => {
            if (error instanceof HttpErrorResponse && (error.status >= 500 || error.status === 0)) {
              return timer(400);
            }
            return throwError(() => error);
          },
        }),
        catchError((err: HttpErrorResponse) => {
          if (err.status !== 404) {
            return throwError(() => err);
          }
          return this.createFromAuthAccount();
        }),
        tap((profile) => {
          this.profile.set(profile);
          this.lookup.remember(profile);
        }),
        finalize(() => {
          this.inflight$ = null;
        }),
        shareReplay(1),
      );
    }
    return this.inflight$;
  }

  private createFromAuthAccount(): Observable<UserProfile> {
    return this.authApi.me().pipe(
      switchMap((auth) =>
        this.users
          .createProfile({
            username: sanitizeUsername(auth.username),
            isPrivate: false,
          })
          .pipe(
            catchError((err: HttpErrorResponse) => {
              if (err.status === 409) {
                return this.users.me();
              }
              return throwError(() => err);
            }),
          ),
      ),
    );
  }
}

function sanitizeUsername(username: string): string {
  const cleaned = username.replace(/[^a-zA-Z0-9._]/g, '_').slice(0, 30);
  if (cleaned.length >= 3) {
    return cleaned;
  }
  return `${cleaned}user`.slice(0, 30);
}
