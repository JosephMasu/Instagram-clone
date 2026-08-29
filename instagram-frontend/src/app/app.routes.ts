import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';
import { Shell } from './layout/shell';
import { LoginPage } from './pages/login/login';
import { RegisterPage } from './pages/register/register';
import { FeedPage } from './pages/feed/feed';
import { PostDetailPage } from './pages/post-detail/post-detail';
import { ProfilePage } from './pages/profile/profile';
import { CreatePostPage } from './pages/create-post/create-post';
import { NotificationsPage } from './pages/notifications/notifications';

export const routes: Routes = [
  { path: 'login', canActivate: [guestGuard], component: LoginPage },
  { path: 'register', canActivate: [guestGuard], component: RegisterPage },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', component: FeedPage },
      { path: 'create', component: CreatePostPage },
      { path: 'notifications', component: NotificationsPage },
      { path: 'p/:postId', component: PostDetailPage },
      { path: 'profile/:username', component: ProfilePage },
    ],
  },
  { path: '**', redirectTo: '' },
];
