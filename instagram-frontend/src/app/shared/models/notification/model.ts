export type NotificationType =
  | 'FOLLOW'
  | 'LIKE'
  | 'COMMENT'
  | 'MENTION'
  | 'COMMENT_REPLY';

export interface AppNotification {
  id: string;
  recipientUserId: string;
  actorUserId: string;
  type: NotificationType;
  postId: string | null;
  commentId: string | null;
  parentCommentId: string | null;
  read: boolean;
  createdAt: string;
}

export interface UnreadCountResponse {
  unreadCount: number;
}
