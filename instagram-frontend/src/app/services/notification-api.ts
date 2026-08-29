import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AppNotification, UnreadCountResponse } from '../shared/models/notification/model';

const BASE = '/api/v1/notifications';

@Injectable({ providedIn: 'root' })
export class NotificationApi {
  private readonly http = inject(HttpClient);

  list() {
    return this.http.get<AppNotification[]>(BASE);
  }

  unreadCount() {
    return this.http.get<UnreadCountResponse>(`${BASE}/unread-count`);
  }

  markRead(id: string) {
    return this.http.patch<AppNotification>(`${BASE}/${id}/read`, {});
  }

  markAllRead() {
    return this.http.post<void>(`${BASE}/read-all`, {});
  }
}
