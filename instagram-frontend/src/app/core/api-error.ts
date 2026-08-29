import { HttpErrorResponse } from '@angular/common/http';

export function apiError(error: unknown, fallback = 'Something went wrong'): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as { message?: string } | string | null;
    if (typeof body === 'string' && body.trimStart().toLowerCase().startsWith('<!doctype')) {
      return 'Login hit the Angular HTML page instead of the API. Restart ng serve after the proxy fix, and keep the gateway on port 8080.';
    }
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (body && typeof body === 'object' && body.message) {
      return body.message;
    }
    if (error.status === 0) {
      return 'Cannot reach the API. Is the gateway running on port 8080?';
    }
    if (error.status >= 500) {
      return fallback;
    }
  }
  return fallback;
}
