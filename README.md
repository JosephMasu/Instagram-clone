# Instagram clone

A full-stack Instagram-style app: **Java 21 / Spring Boot microservices** on the backend and **Angular 22** on the frontend. Not affiliated with Meta.

The work goes from identity and data on the server, through a single API gateway, to an Instagram-like UI that uses those APIs.

```
Angular (localhost:4200)
        │  /api
        ▼
Spring Cloud Gateway (:8080)
        ├── Auth service           :8081   accounts, JWT
        ├── User service           :8082   profiles, follows
        ├── Post service           :8083   posts, likes, comments
        └── Notification service   :8084   inbox

MongoDB (one database per service)     Kafka (likes, comments, follows)
```

---

## Backend

Maven multi-module project: `common-events`, `auth-service`, `user-service`, `post-service`, `notification-service`, `api-gateway`. Each domain service is Spring Boot 4, Spring Web MVC, Spring Security, and Spring Data MongoDB. They share a JWT secret; they do not share databases.

### Auth service

Register, login, refresh, and `/auth/me`. Passwords are hashed with Spring Security. Access tokens are JWTs (JJWT); `sub` is the auth user id. Refresh tokens are stored in Mongo so sessions can be rotated and invalidated. This service does not store bios or follow graphs.

### User service

Profiles: username, name, bio, photo, private flag, follower counts. Create/update `/users/me`, lookup by id or username (id may be Mongo `_id` or `authUserId`). Follow and unfollow; follow events go to Kafka (`user.followed`, `user.unfollowed`). A Kafka consumer can create a profile when an auth account is created.

### Post service

Posts (caption, media URL, image or video), feed, per-user posts, likes, comments and replies. Mentions in comments are resolved by calling user-service. Comment responses include **username** and **profile picture** from that lookup so the UI does not show a generic “user”. Likes and comments publish Kafka events. The same actions also **HTTP POST** to notification-service (with the caller’s Bearer token) so the inbox still updates if Kafka was misconfigured. Duplicate like/comment notifications are skipped when possible. You are not notified for liking or commenting on your own post.

### Notification service

Inbox documents: follow, like, comment, mention, comment reply. List and unread count are scoped to the JWT `sub`. Kafka consumers listen on follow/like/comment topics. HTTP endpoints under `/api/v1/notifications/events` accept like and comment from post-service.

### API gateway

Spring Cloud Gateway (WebFlux) on port 8080. The browser only talks to `/api`. Routes:

- `/api/v1/auth/**` → auth
- `/api/v1/users/*/posts` → post (must be registered before `/users/**`)
- `/api/v1/users/**` → user
- `/api/v1/posts/**` and `/api/v1/comments/**` → post
- `/api/v1/notifications/**` → notifications

Codec size is raised so larger photo data URLs can pass through.

### Data and events

MongoDB 8: `instagram_auth`, `instagram_users`, `instagram_posts`, `instagram_notifications`.

Apache Kafka 4 (KRaft, no ZooKeeper). Shared types live in `common-events`. In Docker, brokers advertise `kafka:29092` to other containers and `localhost:9092` to the host. Advertising `localhost` inside Docker is why consumers used to miss likes and comments.

Docker Compose runs Mongo, Kafka, and all five Java images. Network `instagram-network` is external.

---

## Frontend

`instagram-frontend/` is Angular 22: standalone components, signals, RxJS `HttpClient`, Tailwind CSS 4, Instagram-like layout (sidebar, feed, overlays). Dev server and SSR proxy `/api` to the gateway on 8080.

### Auth and session

Login and register pages. After login, tokens go in `AuthStore` (localStorage); the interceptor attaches `Authorization: Bearer`. On 401, refresh is shared (`shareReplay`) so parallel calls do not log everyone out. Auth guards: guests stay on login/register; the shell requires a token. Register also creates a user-service profile. There is no extra onboarding screen after login.

### App shell and pages

Logged-in routes: feed `/`, create `/create`, post overlay `/p/:postId`, profile `/profile/:username` (and `me`), notifications `/notifications`.

- **Feed** — cards, double-tap like, counts next to icons when non-zero, skeleton while loading.
- **Create** — pick or drop media, optional caption, share; images can be compressed to data URLs (API is URL-only).
- **Profile** — stats, edit, change/remove photo, posts grid, skeletons.
- **Post detail** — comments overlay; commenter **name** from the API, then lookup, then current user / post author. Delete comment (author or post owner); empty 204 responses are handled as text, not JSON.
- **Notifications** — list, unread badge, poll after **client hydration** (`afterNextRender`), not SSR `ngOnInit`. Empty copy only when the API returns no items.

Stores: `CurrentUserStore` (`/users/me`, create profile on 404), `NotificationStore`, `UserLookup` (cache by id and `authUserId`).

---

## How a like reaches the other user

1. User B likes user A’s post (Angular → gateway → post-service).
2. Post-service saves the like, publishes `like.created`, and calls notification-service.
3. Notification-service stores a LIKE for A (skipped if A is B).
4. User A’s shell polls `/api/v1/notifications` and shows the row and badge.

Comments follow the same idea (`comment.created` + HTTP). Follows use Kafka from user-service.

---

## Run locally

Java 21, Maven, Docker, Node 20+.

```bash
cp .env.example .env    # set JWT_SECRET
docker network create instagram-network

docker build -f auth-service/Dockerfile -t instagram-auth-service:latest .
docker build -f user-service/Dockerfile -t instagram-user-service:latest .
docker build -f post-service/Dockerfile -t instagram-post-service:latest .
docker build -f notification-service/Dockerfile -t instagram-notification-service:latest .
docker build -f api-gateway/Dockerfile -t instagram-api-gateway:latest .

docker compose up -d
cd instagram-frontend && npm install && npm start
```

App: http://localhost:4200 · Gateway: http://localhost:8080
