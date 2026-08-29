# Instagram clone

A learning project that models Instagram-style feeds, profiles, likes, comments, and notifications as **separate services**, with a single **API gateway** and an **Angular** client.

This is not Meta’s architecture. It is a bounded-context clone: identity, social graph, posts, and inboxes do not share one database.

## What it does

- Register and log in (JWT access + refresh tokens)
- Profile (username, name, bio, photo)
- Follow / unfollow
- Create image or video posts (media stored as URLs)
- Feed, likes, comments (including replies)
- Notifications when someone else likes, comments, mentions, or follows you

## Stack

| Layer | Choice | Role |
| --- | --- | --- |
| Language | Java 21 | Domain services |
| Framework | Spring Boot 4 | REST APIs, security, Kafka, Mongo |
| Edge | Spring Cloud Gateway (WebFlux) | One `/api` entry point |
| Auth | Spring Security + JJWT | Passwords, JWT `sub` = auth user id |
| Data | MongoDB 8 | One database per service |
| Events | Apache Kafka 4 (KRaft) | Likes, comments, follows as facts |
| UI | Angular 22, TypeScript, signals | SPA + SSR-capable build |
| Styling | Tailwind CSS 4 | Layout |
| HTTP (UI) | Angular `HttpClient`, RxJS | Auth interceptor, token refresh |
| Runtime | Docker Compose | Mongo, Kafka, all Java services |

Frontend lives in `instagram-frontend/`. Backend is a Maven multi-module build at the repo root.

## Architecture

```
Browser (localhost:4200)
    │  /api  (dev proxy or SSR proxy)
    ▼
API Gateway :8080
    ├── Auth            :8081   instagram_auth
    ├── User            :8082   instagram_users
    ├── Post            :8083   instagram_posts
    └── Notifications   :8084   instagram_notifications

User & Post  ──produce──►  Kafka  ──consume──►  Notifications
Post  ──HTTP──►  User (comment author lookup)
Post  ──HTTP──►  Notifications (like/comment, in addition to Kafka)
```

Gateway routes (path order matters for user posts):

| Path | Service |
| --- | --- |
| `/api/v1/auth/**` | Auth |
| `/api/v1/users/*/posts` | Post |
| `/api/v1/users/**` | User |
| `/api/v1/posts/**` | Post |
| `/api/v1/comments/**` | Post |
| `/api/v1/notifications/**` | Notifications |

Kafka (inside Docker) is advertised as `kafka:29092`. The host maps `localhost:9092`. Services in Compose must use `KAFKA_BOOTSTRAP_SERVERS=kafka:29092`. If the broker advertises `localhost` to containers, events never arrive.

## Repository layout

```
auth-service/            credentials, JWT
user-service/            profiles, follows
post-service/            posts, likes, comments
notification-service/    inbox
api-gateway/             routing
common-events/           shared Kafka payload types
instagram-frontend/      Angular app
docker-compose.yml
```

## Prerequisites

- Java 21 and Maven (to build images)
- Docker and Docker Compose
- Node.js 20+ and npm (frontend)
- A Docker network named `instagram-network` (Compose marks it `external`)

## Quick start

### 1. Environment

```bash
cp .env.example .env
```

Set `JWT_SECRET` to a long random string. All JWT-validating services must share it.

### 2. Docker network

```bash
docker network create instagram-network
```

### 3. Build service images

Compose expects pre-built tags (`instagram-auth-service:latest`, and the same pattern for user, post, notification, and gateway). From the repo root:

```bash
docker build -f auth-service/Dockerfile -t instagram-auth-service:latest .
docker build -f user-service/Dockerfile -t instagram-user-service:latest .
docker build -f post-service/Dockerfile -t instagram-post-service:latest .
docker build -f notification-service/Dockerfile -t instagram-notification-service:latest .
docker build -f api-gateway/Dockerfile -t instagram-api-gateway:latest .
```

### 4. Run the backend

```bash
docker compose up -d
```

Gateway: `http://localhost:8080`

### 5. Run the frontend

```bash
cd instagram-frontend
npm install
npm start
```

App: `http://localhost:4200`  
Dev server proxies `/api` to `http://localhost:8080`.

Log in with two browsers (or two accounts) to see likes and comments appear in the other user’s **Notifications**. You do not get notified for your own likes or comments on your own posts.

## Ports

| What | Port |
| --- | --- |
| Angular | 4200 |
| API gateway | 8080 |
| Auth | 8081 |
| User | 8082 |
| Post | 8083 |
| Notifications | 8084 |
| MongoDB | 27017 |
| Kafka (host) | 9092 |

## Frontend routes

| Path | Page |
| --- | --- |
| `/login`, `/register` | Guest |
| `/` | Feed |
| `/create` | New post |
| `/p/:postId` | Post + comments overlay |
| `/profile/:username` | Profile (`me` for the current user) |
| `/notifications` | Activity |

## Design notes

- **JWT `sub`** is the auth-service user id. Profiles store `authUserId`. Lookups often accept either id.
- **Media** is a URL (the UI may compress a file into a data URL). There is no S3-style object store.
- **Notifications** are written from Kafka consumers and from post-service HTTP calls. Duplicate likes/comments are ignored where possible.
- **Unread badge** on the shell polls the notifications API on a short interval after the client has hydrated (not during SSR `ngOnInit`).

## Limitations

- Shared JWT secret, not rotated keys or an API-gateway auth filter
- Polling instead of WebSockets / SSE
- Large photos can stress the gateway codec if they stay as data URLs
- Local Compose Kafka is not a production cluster

## License

Add a license if you publish the repo (for example MIT). Until then, treat the code as private unless you say otherwise.
