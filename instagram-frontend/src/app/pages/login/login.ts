import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';
import { AuthApi } from '../../services/auth-api';
import { AuthStore } from '../../core/auth.store';
import { CurrentUserStore } from '../../core/current-user.store';
import { apiError } from '../../core/api-error';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly authApi = inject(AuthApi);
  private readonly store = inject(AuthStore);
  private readonly currentUser = inject(CurrentUserStore);
  private readonly router = inject(Router);

  readonly error = signal('');
  readonly pending = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.error.set('');
    this.authApi
      .login(this.form.getRawValue())
      .pipe(
        tap((tokens) => {
          this.currentUser.clear();
          this.store.setTokens(tokens.accessToken, tokens.refreshToken);
        }),
        switchMap(() => this.currentUser.ensureProfile().pipe(catchError(() => of(null)))),
      )
      .subscribe({
        next: () => {
          this.pending.set(false);
          void this.router.navigateByUrl('/');
        },
        error: (err) => {
          this.pending.set(false);
          this.error.set(
            apiError(err, 'Sorry, your password was incorrect. Please double-check your password.'),
          );
        },
      });
  }
}
