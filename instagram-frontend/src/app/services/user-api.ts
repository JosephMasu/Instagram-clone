import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {
  CreateProfileRequest,
  UpdateProfileRequest,
  UserProfile,
} from '../shared/models/user/model';

const BASE = '/api/v1/users';

@Injectable({ providedIn: 'root' })
export class UserApi {
  private readonly http = inject(HttpClient);

  createProfile(body: CreateProfileRequest) {
    return this.http.post<UserProfile>(BASE, body);
  }

  me() {
    return this.http.get<UserProfile>(`${BASE}/me`);
  }

  updateMe(body: UpdateProfileRequest) {
    return this.http.put<UserProfile>(`${BASE}/me`, body);
  }

  byUsername(username: string) {
    return this.http.get<UserProfile>(`${BASE}/username/${username}`);
  }

  byId(userId: string) {
    return this.http.get<UserProfile>(`${BASE}/${userId}`);
  }

  follow(userId: string) {
    return this.http.post<void>(`${BASE}/${userId}/follow`, {});
  }

  unfollow(userId: string) {
    return this.http.delete<void>(`${BASE}/${userId}/follow`);
  }

  followers(userId: string) {
    return this.http.get<UserProfile[]>(`${BASE}/${userId}/followers`);
  }

  following(userId: string) {
    return this.http.get<UserProfile[]>(`${BASE}/${userId}/following`);
  }
}
