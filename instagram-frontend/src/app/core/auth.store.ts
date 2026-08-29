import { Injectable, computed, inject, signal } from '@angular/core';
import { PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthState } from '../shared/models/auth/model';

const ACCESS = 'ig_access_token';
const REFRESH = 'ig_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly browser = isPlatformBrowser(this.platformId);

  private readonly state = signal<AuthState>(this.read());

  readonly accessToken = computed(() => this.state().accessToken);
  readonly refreshToken = computed(() => this.state().refreshToken);
  readonly isAuthenticated = computed(() => this.state().isAuthenticated);

  private logoutListeners = new Set<() => void>();

  onLogout(listener: () => void): void {
    this.logoutListeners.add(listener);
  }

  currentAccessToken(): string | null {
    return this.state().accessToken;
  }

  setTokens(accessToken: string, refreshToken: string): void {
    this.write({ accessToken, refreshToken, isAuthenticated: true });
  }

  logout(): void {
    for (const listener of this.logoutListeners) {
      listener();
    }
    this.write({ accessToken: null, refreshToken: null, isAuthenticated: false });
  }

  private read(): AuthState {
    if (!this.browser) {
      return { accessToken: null, refreshToken: null, isAuthenticated: false };
    }
    const accessToken = localStorage.getItem(ACCESS);
    const refreshToken = localStorage.getItem(REFRESH);
    return {
      accessToken,
      refreshToken,
      isAuthenticated: Boolean(accessToken),
    };
  }

  private write(next: AuthState): void {
    this.state.set(next);
    if (!this.browser) {
      return;
    }
    if (next.accessToken && next.refreshToken) {
      localStorage.setItem(ACCESS, next.accessToken);
      localStorage.setItem(REFRESH, next.refreshToken);
    } else {
      localStorage.removeItem(ACCESS);
      localStorage.removeItem(REFRESH);
    }
  }
}
