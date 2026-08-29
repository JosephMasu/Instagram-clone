import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { interval } from 'rxjs';
import { NotificationApi } from '../services/notification-api';
import { UserLookup } from '../services/user-lookup';
import { AppNotification } from '../shared/models/notification/model';
import { UserProfile } from '../shared/models/user/model';
import { AuthStore } from './auth.store';
import { apiError } from './api-error';

@Injectable({ providedIn: 'root' })
export class NotificationStore {
  private readonly api = inject(NotificationApi);
  private readonly lookup = inject(UserLookup);
  private readonly auth = inject(AuthStore);
  private readonly browser = isPlatformBrowser(inject(PLATFORM_ID));
  private started = false;

  readonly items = signal<AppNotification[]>([]);
  readonly actors = signal<Record<string, UserProfile>>({});
  readonly unreadCount = signal(0);
  readonly loading = signal(false);
  readonly error = signal('');

  constructor() {
    this.auth.onLogout(() => this.clear());
  }

  start(): void {
    if (!this.browser || this.started) {
      return;
    }
    this.started = true;
    this.refresh();
    interval(8000).subscribe(() => this.refresh());
  }

  refresh(): void {
    if (!this.browser || !this.auth.isAuthenticated()) {
      return;
    }
    if (!this.items().length) {
      this.loading.set(true);
    }
    this.api.list().subscribe({
      next: (items) => {
        this.items.set(items);
        this.unreadCount.set(items.filter((item) => !item.read).length);
        this.error.set('');
        this.loading.set(false);
        const ids = [...new Set(items.map((item) => item.actorUserId))];
        if (!ids.length) {
          return;
        }
        this.lookup.byIds(ids).subscribe((actors) => this.actors.set(actors));
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiError(err, 'Could not load notifications'));
      },
    });
  }

  markRead(item: AppNotification): void {
    if (item.read) {
      return;
    }
    this.api.markRead(item.id).subscribe({
      next: (updated) => {
        this.items.update((list) => list.map((n) => (n.id === updated.id ? updated : n)));
        this.unreadCount.set(this.items().filter((n) => !n.read).length);
      },
    });
  }

  markAllRead(): void {
    this.api.markAllRead().subscribe({
      next: () => this.refresh(),
    });
  }

  actor(id: string): UserProfile | undefined {
    return this.actors()[id];
  }

  private clear(): void {
    this.items.set([]);
    this.actors.set({});
    this.unreadCount.set(0);
    this.error.set('');
    this.loading.set(false);
  }
}
