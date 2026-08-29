import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Comment, CreateCommentRequest, CreatePostRequest, Post } from '../shared/models/post/model';

@Injectable({ providedIn: 'root' })
export class PostApi {
  private readonly http = inject(HttpClient);

  feed() {
    return this.http.get<Post[]>('/api/v1/posts');
  }

  get(postId: string) {
    return this.http.get<Post>(`/api/v1/posts/${postId}`);
  }

  create(body: CreatePostRequest) {
    return this.http.post<Post>('/api/v1/posts', body);
  }

  byUser(userId: string) {
    return this.http.get<Post[]>(`/api/v1/users/${userId}/posts`);
  }

  like(postId: string) {
    return this.http.post(`/api/v1/posts/${postId}/like`, {}, { responseType: 'text' });
  }

  unlike(postId: string) {
    return this.http.delete(`/api/v1/posts/${postId}/like`, { responseType: 'text' });
  }

  comments(postId: string) {
    return this.http.get<Comment[]>(`/api/v1/posts/${postId}/comments`);
  }

  addComment(postId: string, body: CreateCommentRequest) {
    return this.http.post<Comment>(`/api/v1/posts/${postId}/comments`, body);
  }

  deleteComment(commentId: string) {
    return this.http.delete(`/api/v1/comments/${commentId}`, { responseType: 'text' });
  }
}
