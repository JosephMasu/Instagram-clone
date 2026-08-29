import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../shared/models/auth/model';

const BASE = '/api/v1/auth';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(body: LoginRequest) {
    return this.http.post<AuthResponse>(`${BASE}/login`, body);
  }

  register(body: RegisterRequest) {
    return this.http.post<AuthUser>(`${BASE}/register`, body);
  }

  refresh(refreshToken: string) {
    return this.http.post<AuthResponse>(`${BASE}/refresh`, { refreshToken });
  }

  me() {
    return this.http.get<AuthUser>(`${BASE}/me`);
  }
}
