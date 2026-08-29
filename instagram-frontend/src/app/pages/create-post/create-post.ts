import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PostApi } from '../../services/post-api';
import { CurrentUserStore } from '../../core/current-user.store';
import { apiError } from '../../core/api-error';
import { MediaType } from '../../shared/models/post/model';
import { mediaFromFile } from '../../shared/media';
import { Avatar } from '../../shared/avatar';

@Component({
  selector: 'app-create-post',
  imports: [FormsModule, Avatar],
  templateUrl: './create-post.html',
})
export class CreatePostPage {
  private readonly posts = inject(PostApi);
  readonly router = inject(Router);
  readonly me = inject(CurrentUserStore);

  readonly error = signal('');
  readonly pending = signal(false);
  readonly dragging = signal(false);
  readonly preview = signal('');
  readonly mediaType = signal<MediaType>('IMAGE');
  caption = '';

  constructor() {
    this.me.ensureProfile().subscribe({ error: () => undefined });
  }

  async onFiles(files: FileList | null): Promise<void> {
    const file = files?.item(0);
    if (!file) {
      return;
    }
    this.error.set('');
    try {
      const media = await mediaFromFile(file, 1080);
      this.preview.set(media.dataUrl);
      this.mediaType.set(media.mediaType);
    } catch (err) {
      this.error.set(err instanceof Error ? err.message : 'Could not use that file');
    }
  }

  onInput(event: Event): void {
    void this.onFiles((event.target as HTMLInputElement).files);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(false);
    void this.onFiles(event.dataTransfer?.files ?? null);
  }

  discard(): void {
    this.preview.set('');
    this.caption = '';
    this.error.set('');
  }

  share(): void {
    const mediaUrl = this.preview();
    if (!mediaUrl) {
      return;
    }
    this.pending.set(true);
    this.error.set('');
    this.posts
      .create({
        caption: this.caption.trim(),
        mediaUrl,
        mediaType: this.mediaType(),
      })
      .subscribe({
        next: (post) => {
          this.pending.set(false);
          void this.router.navigateByUrl('/');
        },
        error: (err) => {
          this.pending.set(false);
          this.error.set(apiError(err, 'Could not share'));
        },
      });
  }
}
