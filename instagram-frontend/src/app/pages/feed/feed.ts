import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostApi } from '../../services/post-api';
import { UserLookup } from '../../services/user-lookup';
import { CurrentUserStore } from '../../core/current-user.store';
import { Post } from '../../shared/models/post/model';
import { UserProfile } from '../../shared/models/user/model';
import { apiError } from '../../core/api-error';
import { Avatar } from '../../shared/avatar';
import { TimeAgoPipe } from '../../shared/time-ago.pipe';

@Component({
  selector: 'app-feed',
  imports: [RouterLink, Avatar, TimeAgoPipe],
  templateUrl: './feed.html',
})
export class FeedPage implements OnInit {
  private readonly postsApi = inject(PostApi);
  private readonly lookup = inject(UserLookup);
  readonly me = inject(CurrentUserStore);

  readonly posts = signal<Post[]>([]);
  readonly authors = signal<Record<string, UserProfile>>({});
  readonly error = signal('');
  readonly loading = signal(true);
  readonly likedBurst = signal<string | null>(null);
  readonly saved = signal<Record<string, boolean>>({});

  ngOnInit(): void {
    this.me.ensureProfile().subscribe({ error: () => undefined });
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set('');
    this.postsApi.feed().subscribe({
      next: (posts) => {
        this.posts.set(posts);
        const ids = [...new Set(posts.map((p) => p.userId))];
        if (!ids.length) {
          this.authors.set({});
          this.loading.set(false);
          return;
        }
        this.lookup.byIds(ids).subscribe((authors) => {
          this.authors.set(authors);
          this.loading.set(false);
        });
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(apiError(err, 'Could not load feed'));
      },
    });
  }

  author(post: Post): UserProfile | undefined {
    return this.authors()[post.userId];
  }

  toggleLike(post: Post, burst = false): void {
    const req = post.isLiked ? this.postsApi.unlike(post.id) : this.postsApi.like(post.id);
    if (burst && !post.isLiked) {
      this.likedBurst.set(post.id);
      setTimeout(() => {
        if (this.likedBurst() === post.id) {
          this.likedBurst.set(null);
        }
      }, 1000);
    }
    req.subscribe({
      next: () => {
        this.posts.update((list) =>
          list.map((item) =>
            item.id === post.id
              ? {
                  ...item,
                  isLiked: !item.isLiked,
                  likeCount: item.likeCount + (item.isLiked ? -1 : 1),
                }
              : item,
          ),
        );
      },
    });
  }

  onMediaDblClick(post: Post): void {
    if (!post.isLiked) {
      this.toggleLike(post, true);
    } else {
      this.likedBurst.set(post.id);
      setTimeout(() => {
        if (this.likedBurst() === post.id) {
          this.likedBurst.set(null);
        }
      }, 1000);
    }
  }

  toggleSave(postId: string): void {
    this.saved.update((map) => ({ ...map, [postId]: !map[postId] }));
  }
}
