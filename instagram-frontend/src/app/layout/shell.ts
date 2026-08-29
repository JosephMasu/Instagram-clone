import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthStore } from '../core/auth.store';
import { CurrentUserStore } from '../core/current-user.store';
import { Avatar } from '../shared/avatar';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Avatar],
  templateUrl: './shell.html',
})
export class Shell implements OnInit {
  readonly router = inject(Router);
  readonly auth = inject(AuthStore);
  readonly me = inject(CurrentUserStore);

  ngOnInit(): void {
    this.me.ensureProfile().subscribe({ error: () => undefined });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
