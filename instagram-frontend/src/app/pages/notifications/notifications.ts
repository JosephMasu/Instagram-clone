import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationApi } from '../../services/notification-api';
import { UserLookup } from '../../services/user-lookup';
import { AppNotification } from '../../shared/models/notification/model';
import { UserProfile } from '../../shared/models/user/model';
import { Avatar } from '../../shared/avatar';
import { TimeAgoPipe } from '../../shared/time-ago.pipe';

@Component({
  selector: 'app-notifications',
  imports: [RouterLink, Avatar, TimeAgoPipe],
  templateUrl: './notifications.html',
})
export class NotificationsPage implements OnInit {
  private readonly api = inject(NotificationApi);
  private readonly lookup = inject(UserLookup);

  readonly items = signal<AppNotification[]>([]);
  readonly actors = signal<Record<string, UserProfile>>({});
  readonly error = signal('');
  readonly loading = signal(true);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set('');
    this.api
      .list()
      .pipe(catchError(() => of([] as AppNotification[])))
      .subscribe((items) => {
        this.items.set(items);
        this.loading.set(false);
        const ids = [...new Set(items.map((i) => i.actorUserId))];
        if (!ids.length) {
          return;
        }
        this.lookup.byIds(ids).subscribe((actors) => this.actors.set(actors));
      });
  }

  actor(id: string): UserProfile | undefined {
    return this.actors()[id];
  }

  markRead(item: AppNotification): void {
    if (item.read) {
      return;
    }
    this.api.markRead(item.id).subscribe({
      next: (updated) =>
        this.items.update((list) => list.map((n) => (n.id === updated.id ? updated : n))),
    });
  }

  markAll(): void {
    this.api.markAllRead().subscribe({
      next: () => this.reload(),
    });
  }

  label(item: AppNotification): string {
    switch (item.type) {
      case 'FOLLOW':
        return 'started following you.';
      case 'LIKE':
        return 'liked your photo.';
      case 'COMMENT':
        return 'commented on your post.';
      case 'MENTION':
        return 'mentioned you in a comment.';
      case 'COMMENT_REPLY':
        return 'replied to your comment.';
      default:
        return item.type;
    }
  }
}
