import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';
import { AuthApi } from '../../services/auth-api';
import { UserApi } from '../../services/user-api';
import { AuthStore } from '../../core/auth.store';
import { CurrentUserStore } from '../../core/current-user.store';
import { apiError } from '../../core/api-error';
import { UserProfile } from '../../shared/models/user/model';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
})
export class RegisterPage {
  private readonly fb = inject(FormBuilder);
  private readonly authApi = inject(AuthApi);
  private readonly users = inject(UserApi);
  private readonly store = inject(AuthStore);
  private readonly currentUser = inject(CurrentUserStore);
  private readonly router = inject(Router);

  readonly error = signal('');
  readonly pending = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    fullName: [''],
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.error.set('');
    const { email, fullName, username, password } = this.form.getRawValue();
    const names = splitFullName(fullName);

    this.authApi
      .register({ username, email, password })
      .pipe(
        switchMap(() => this.authApi.login({ email, password })),
        tap((tokens) => {
          this.currentUser.clear();
          this.store.setTokens(tokens.accessToken, tokens.refreshToken);
        }),
        switchMap(() =>
          this.users
            .createProfile({
              username,
              firstName: names.firstName,
              lastName: names.lastName,
              isPrivate: false,
            })
            .pipe(catchError(() => of(null as UserProfile | null))),
        ),
      )
      .subscribe({
        next: (profile) => {
          if (profile) {
            this.currentUser.set(profile);
          }
          this.pending.set(false);
          void this.router.navigateByUrl('/');
        },
        error: (err) => {
          this.pending.set(false);
          this.error.set(apiError(err, 'Could not sign up'));
        },
      });
  }
}

function splitFullName(fullName: string): { firstName?: string; lastName?: string } {
  const parts = fullName.trim().split(/\s+/).filter(Boolean);
  if (!parts.length) {
    return {};
  }
  if (parts.length === 1) {
    return { firstName: parts[0] };
  }
  return { firstName: parts[0], lastName: parts.slice(1).join(' ') };
}
