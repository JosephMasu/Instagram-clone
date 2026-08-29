import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { UserApi } from './user-api';
import { UserProfile } from '../shared/models/user/model';

@Injectable({ providedIn: 'root' })
export class UserLookup {
  private readonly users = inject(UserApi);
  private readonly cache: Record<string, UserProfile> = {};

  remember(profile: UserProfile | null | undefined): void {
    if (!profile) {
      return;
    }
    this.cache[profile.id] = profile;
    this.cache[profile.authUserId] = profile;
  }

  byIds(ids: string[]): Observable<Record<string, UserProfile>> {
    const unique = [...new Set(ids.filter(Boolean))];
    if (!unique.length) {
      return of({});
    }
    const missing = unique.filter((id) => !this.cache[id]);
    const fromCache = () => {
      const map: Record<string, UserProfile> = {};
      for (const id of unique) {
        const profile = this.cache[id];
        if (profile) {
          map[id] = profile;
          map[profile.id] = profile;
          map[profile.authUserId] = profile;
        }
      }
      return map;
    };
    if (!missing.length) {
      return of(fromCache());
    }
    return forkJoin(
      missing.map((id) =>
        this.users.byId(id).pipe(
          catchError(() => of(null)),
          map((profile) => ({ id, profile })),
        ),
      ),
    ).pipe(
      tap((rows) => {
        for (const row of rows) {
          this.remember(row.profile);
        }
      }),
      map(() => fromCache()),
    );
  }
}
