import { Component, computed, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';
import { CurrentUserStore } from '../../core/current-user.store';
import { UserApi } from '../../services/user-api';
import { PostApi } from '../../services/post-api';
import { UserProfile } from '../../shared/models/user/model';
import { Post } from '../../shared/models/post/model';
import { apiError } from '../../core/api-error';
import { mediaFromFile } from '../../shared/media';
import { Avatar } from '../../shared/avatar';

@Component({
  selector: 'app-profile',
  imports: [RouterLink, ReactiveFormsModule, Avatar],
  templateUrl: './profile.html',
})
export class ProfilePage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly users = inject(UserApi);
  private readonly postsApi = inject(PostApi);
  private readonly currentUser = inject(CurrentUserStore);
  private readonly fb = inject(FormBuilder);

  readonly profile = signal<UserProfile | null>(null);
  readonly posts = signal<Post[]>([]);
  readonly error = signal('');
  readonly loading = signal(true);
  readonly editing = signal(false);
  readonly photoMenu = signal(false);
  readonly tab = signal<'posts' | 'saved' | 'tagged'>('posts');
  readonly saving = signal(false);
  readonly photoBusy = signal(false);
  private readonly photoInput = viewChild<ElementRef<HTMLInputElement>>('photoInput');

  readonly isOwn = computed(() => {
    const me = this.currentUser.profile();
    const profile = this.profile();
    return Boolean(me && profile && me.id === profile.id);
  });

  readonly displayName = computed(() => {
    const p = this.profile();
    if (!p) {
      return '';
    }
    return [p.firstName, p.lastName].filter(Boolean).join(' ');
  });

  readonly editForm = this.fb.nonNullable.group({
    firstName: [''],
    lastName: [''],
    bio: [''],
    profilePictureUrl: [''],
  });

  ngOnInit(): void {
    this.currentUser.ensureProfile().subscribe({ error: () => undefined });
    this.route.paramMap.subscribe((params) => this.load(params.get('username') ?? 'me'));
  }

  retry(): void {
    const username = this.route.snapshot.paramMap.get('username') ?? 'me';
    if (username === 'me') {
      this.currentUser.clear();
    }
    this.load(username);
  }

  private load(username: string): void {
    this.loading.set(true);
    this.error.set('');
    this.tab.set('posts');
    const profile$ =
      username === 'me' ? this.currentUser.ensureProfile() : this.users.byUsername(username);
    profile$
      .pipe(
        switchMap((profile) =>
          this.postsApi.byUser(profile.authUserId).pipe(
            catchError(() => of([] as Post[])),
            map((posts) => ({ profile, posts })),
          ),
        ),
      )
      .subscribe({
        next: ({ profile, posts }) => {
          this.profile.set(profile);
          this.posts.set(posts);
          this.editForm.patchValue({
            firstName: profile.firstName ?? '',
            lastName: profile.lastName ?? '',
            bio: profile.bio ?? '',
            profilePictureUrl: profile.profilePictureUrl ?? '',
          });
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(apiError(err, 'Profile not found'));
        },
      });
  }

  openPhotoMenu(): void {
    if (this.isOwn()) {
      this.photoMenu.set(true);
    }
  }

  openPhotoPicker(): void {
    this.photoInput()?.nativeElement.click();
  }

  async onPhotoPicked(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    input.value = '';
    if (!file) {
      return;
    }
    this.photoBusy.set(true);
    try {
      const media = await mediaFromFile(file, 480);
      this.applyPhoto(media.dataUrl);
    } catch (err) {
      this.photoBusy.set(false);
      this.error.set(err instanceof Error ? err.message : 'Could not use that photo');
    }
  }

  removePhoto(): void {
    this.applyPhoto('');
  }

  private applyPhoto(url: string): void {
    this.photoBusy.set(true);
    this.users.updateMe({ profilePictureUrl: url }).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.currentUser.set(profile);
        this.editForm.patchValue({ profilePictureUrl: url });
        this.photoMenu.set(false);
        this.photoBusy.set(false);
      },
      error: () => this.photoBusy.set(false),
    });
  }

  toggleFollow(): void {
    const profile = this.profile();
    if (!profile) {
      return;
    }
    const req = profile.isFollowing
      ? this.users.unfollow(profile.id)
      : this.users.follow(profile.id);
    req.subscribe({
      next: () =>
        this.profile.set({
          ...profile,
          isFollowing: !profile.isFollowing,
          followerCount: profile.followerCount + (profile.isFollowing ? -1 : 1),
        }),
    });
  }

  saveProfile(): void {
    const v = this.editForm.getRawValue();
    this.saving.set(true);
    this.users
      .updateMe({
        firstName: v.firstName || undefined,
        lastName: v.lastName || undefined,
        bio: v.bio || undefined,
        profilePictureUrl: v.profilePictureUrl || undefined,
      })
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.currentUser.set(profile);
          this.editing.set(false);
          this.saving.set(false);
        },
        error: () => this.saving.set(false),
      });
  }
}
