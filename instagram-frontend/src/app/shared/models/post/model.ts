export type MediaType = 'IMAGE' | 'VIDEO';

export interface Post {
  id: string;
  userId: string;
  caption: string;
  mediaUrl: string;
  mediaType: MediaType;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
}

export interface CreatePostRequest {
  caption: string;
  mediaUrl: string;
  mediaType: MediaType;
}

export interface Comment {
  id: string;
  postId: string;
  userId: string;
  username?: string | null;
  profilePictureUrl?: string | null;
  parentCommentId: string | null;
  text: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCommentRequest {
  text: string;
  parentCommentId?: string | null;
}
