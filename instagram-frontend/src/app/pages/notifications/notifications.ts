import { afterNextRender, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NotificationStore } from '../../core/notification.store';
import { AppNotification } from '../../shared/models/notification/model';
import { Avatar } from '../../shared/avatar';
import { TimeAgoPipe } from '../../shared/time-ago.pipe';

@Component({
  selector: 'app-notifications',
  imports: [RouterLink, Avatar, TimeAgoPipe],
  templateUrl: './notifications.html',
})
export class NotificationsPage {
  readonly store = inject(NotificationStore);

  constructor() {
    afterNextRender(() => this.store.refresh());
  }

  markRead(item: AppNotification): void {
    this.store.markRead(item);
  }

  markAll(): void {
    this.store.markAllRead();
  }

  actor(id: string) {
    return this.store.actor(id);
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
