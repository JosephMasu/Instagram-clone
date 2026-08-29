import { Location } from '@angular/common';
import {
  Component,
  effect,
  ElementRef,
  inject,
  OnInit,
  signal,
  viewChild,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PostApi } from '../../services/post-api';
import { UserLookup } from '../../services/user-lookup';
import { CurrentUserStore } from '../../core/current-user.store';
import { Comment, Post } from '../../shared/models/post/model';
import { UserProfile } from '../../shared/models/user/model';
import { apiError } from '../../core/api-error';
import { Avatar } from '../../shared/avatar';
import { TimeAgoPipe } from '../../shared/time-ago.pipe';

@Component({
  selector: 'app-post-detail',
  imports: [RouterLink, ReactiveFormsModule, Avatar, TimeAgoPipe],
  templateUrl: './post-detail.html',
})
export class PostDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly postsApi = inject(PostApi);
  private readonly lookup = inject(UserLookup);
  readonly me = inject(CurrentUserStore);
  private readonly fb = inject(FormBuilder);
  private readonly commentBox = viewChild<ElementRef<HTMLInputElement>>('commentBox');
  private focusedComposer = false;

  readonly post = signal<Post | null>(null);
  readonly comments = signal<Comment[]>([]);
  readonly commentsReady = signal(false);
  readonly author = signal<UserProfile | null>(null);
  readonly commenters = signal<Record<string, UserProfile>>({});
  readonly error = signal('');
  readonly replyTo = signal<Comment | null>(null);
  readonly likedBurst = signal(false);
  readonly menuFor = signal<Comment | null>(null);
  readonly deleting = signal(false);

  readonly form = this.fb.nonNullable.group({
    text: ['', [Validators.required, Validators.maxLength(1000)]],
  });

  constructor() {
    effect(() => {
      if (this.focusedComposer) {
        return;
      }
      if (this.post() && this.commentBox() && this.route.snapshot.queryParamMap.get('focus') === 'comment') {
        this.focusedComposer = true;
        queueMicrotask(() => this.commentBox()?.nativeElement.focus());
      }
    });
  }

  ngOnInit(): void {
    this.me.ensureProfile().subscribe({ error: () => undefined });
    this.route.paramMap.subscribe((params) => {
      const id = params.get('postId');
      if (!id) {
        return;
      }
      this.focusedComposer = false;
      this.error.set('');
      this.post.set(null);
      this.comments.set([]);
      this.commentsReady.set(false);
      this.postsApi.get(id).subscribe({
        next: (post) => {
          this.post.set(post);
          this.lookup.byIds([post.userId]).subscribe((map) => {
            const profile = map[post.userId] ?? null;
            if (profile) {
              this.lookup.remember(profile);
            }
            this.author.set(profile);
          });
        },
        error: (err) => this.error.set(apiError(err, 'Post not found')),
      });
      this.postsApi
        .comments(id)
        .pipe(catchError(() => of([] as Comment[])))
        .subscribe((comments) => {
          this.comments.set(comments);
          this.commentsReady.set(true);
          this.loadCommenters(comments);
        });
    });
  }

  close(): void {
    const previous = this.router.lastSuccessfulNavigation()?.previousNavigation;
    if (previous) {
      this.location.back();
      return;
    }
    void this.router.navigateByUrl('/');
  }

  private loadCommenters(comments: Comment[]): void {
    this.lookup.byIds(comments.map((c) => c.userId)).subscribe((map) => this.commenters.set(map));
  }

  commenter(userId: string): UserProfile | undefined {
    return this.commenters()[userId];
  }

  commentName(comment: Comment): string {
    const fromApi = comment.username?.trim();
    if (fromApi) {
      return fromApi;
    }
    const looked = this.commenter(comment.userId)?.username;
    if (looked) {
      return looked;
    }
    const me = this.me.profile();
    if (me && (me.authUserId === comment.userId || me.id === comment.userId)) {
      return me.username;
    }
    const author = this.author();
    if (author && (author.authUserId === comment.userId || author.id === comment.userId)) {
      return author.username;
    }
    return 'user';
  }

  commentHandle(comment: Comment): string {
    const name = this.commentName(comment);
    return name === 'user' ? 'me' : name;
  }

  commentPhoto(comment: Comment): string | null | undefined {
    return (
      comment.profilePictureUrl ||
      this.commenter(comment.userId)?.profilePictureUrl ||
      this.commentAvatarFallback(comment.userId)
    );
  }

  private commentAvatarFallback(userId: string): string | null | undefined {
    const me = this.me.profile();
    if (me && (me.authUserId === userId || me.id === userId)) {
      return me.profilePictureUrl;
    }
    const author = this.author();
    if (author && (author.authUserId === userId || author.id === userId)) {
      return author.profilePictureUrl;
    }
    return undefined;
  }

  canDelete(comment: Comment): boolean {
    const me = this.me.profile();
    const post = this.post();
    if (!me) {
      return false;
    }
    const mine =
      me.authUserId === comment.userId || me.id === comment.userId;
    const ownPost = Boolean(
      post && (me.authUserId === post.userId || me.id === post.userId),
    );
    return mine || ownPost;
  }

  toggleLike(): void {
    const post = this.post();
    if (!post) {
      return;
    }
    const req = post.isLiked ? this.postsApi.unlike(post.id) : this.postsApi.like(post.id);
    req.subscribe({
      next: () =>
        this.post.set({
          ...post,
          isLiked: !post.isLiked,
          likeCount: post.likeCount + (post.isLiked ? -1 : 1),
        }),
    });
  }

  onMediaDblClick(): void {
    const post = this.post();
    if (!post) {
      return;
    }
    this.likedBurst.set(true);
    setTimeout(() => this.likedBurst.set(false), 1000);
    if (!post.isLiked) {
      this.toggleLike();
    }
  }

  focusComposer(): void {
    this.commentBox()?.nativeElement.focus();
  }

  addComment(): void {
    const post = this.post();
    if (!post || this.form.invalid) {
      return;
    }
    const parent = this.replyTo();
    this.postsApi
      .addComment(post.id, {
        text: this.form.controls.text.value,
        parentCommentId: parent?.id,
      })
      .subscribe({
        next: (comment) => {
          const me = this.me.profile();
          const hydrated: Comment = {
            ...comment,
            username: comment.username || me?.username,
            profilePictureUrl: comment.profilePictureUrl ?? me?.profilePictureUrl,
          };
          if (me) {
            this.lookup.remember(me);
          }
          this.comments.update((list) => [...list, hydrated]);
          this.loadCommenters(this.comments());
          this.form.reset({ text: '' });
          this.replyTo.set(null);
          this.post.set({ ...post, commentCount: post.commentCount + 1 });
        },
      });
  }

  confirmDelete(): void {
    const comment = this.menuFor();
    if (!comment) {
      return;
    }
    this.deleting.set(true);
    this.postsApi.deleteComment(comment.id).subscribe({
      next: () => {
        const extraReplies = this.comments().filter((c) => c.parentCommentId === comment.id).length;
        this.comments.update((list) =>
          list.filter((c) => c.id !== comment.id && c.parentCommentId !== comment.id),
        );
        const post = this.post();
        if (post) {
          this.post.set({
            ...post,
            commentCount: Math.max(0, post.commentCount - 1 - extraReplies),
          });
        }
        this.menuFor.set(null);
        this.deleting.set(false);
      },
      error: (err) => {
        this.deleting.set(false);
        this.menuFor.set(null);
        this.error.set(apiError(err, 'Could not delete comment'));
      },
    });
  }
}
