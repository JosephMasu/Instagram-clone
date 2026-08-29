# Instagram clone

**Spring Boot microservices + Kafka + MongoDB + Angular** — a full-stack clone of Instagram-style auth, profiles, posts, likes, comments, and notifications.

Not affiliated with Meta. This is a learning architecture: each domain owns its own database, the UI talks to one gateway, and social actions fan out as events.

[Java 21](https://openjdk.org/) · [Spring Boot 4](https://spring.io/projects/spring-boot) · [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) · [Apache Kafka](https://kafka.apache.org/) · [MongoDB](https://www.mongodb.com/) · [Angular 22](https://angular.dev/) · [Docker](https://www.docker.com/)

## Why this repo

A feed looks like one app. Underneath, **who you are**, **who you follow**, **what you posted**, and **who should be notified** are different problems. This project splits them into services so you can see HTTP commands, JWT identity, and Kafka (plus a direct notify call) working together.

## Features

- Register / login with JWT access and refresh tokens
- Profiles (username, bio, photo), follow / unfollow
- Image and video posts, feed, likes, comments and replies
- Notifications when someone else likes, comments, mentions, or follows you

## Architecture

```
Browser (:4200)
    │  /api
    ▼
API Gateway (:8080)
    ├── Auth            :8081    instagram_auth
    ├── User            :8082    instagram_users
    ├── Post            :8083    instagram_posts
    └── Notifications   :8084    instagram_notifications

User & Post  ──Kafka──►  Notifications
Post  ──HTTP──►  User (resolve comment authors)
Post  ──HTTP──►  Notifications (likes/comments)
```

| Path | Service |
| --- | --- |
| `/api/v1/auth/**` | Auth |
| `/api/v1/users/*/posts` | Post |
| `/api/v1/users/**` | User |
| `/api/v1/posts/**`, `/api/v1/comments/**` | Post |
| `/api/v1/notifications/**` | Notifications |

Kafka inside Docker listens on `kafka:29092`. The host uses `localhost:9092`. If the broker advertises `localhost` to containers, events never reach consumers.

## Layout

```
auth-service/            credentials, JWT
user-service/            profiles, follows
post-service/            posts, likes, comments
notification-service/    inbox
api-gateway/             routing
common-events/           shared event types
instagram-frontend/      Angular app
```

## Quick start

**Need:** Java 21, Maven, Docker, Node 20+, npm.

```bash
cp .env.example .env          # set JWT_SECRET to a long random string
docker network create instagram-network

docker build -f auth-service/Dockerfile -t instagram-auth-service:latest .
docker build -f user-service/Dockerfile -t instagram-user-service:latest .
docker build -f post-service/Dockerfile -t instagram-post-service:latest .
docker build -f notification-service/Dockerfile -t instagram-notification-service:latest .
docker build -f api-gateway/Dockerfile -t instagram-api-gateway:latest .

docker compose up -d

cd instagram-frontend && npm install && npm start
```

- App: http://localhost:4200  
- Gateway: http://localhost:8080  

`ng serve` proxies `/api` to the gateway. Use **two accounts** to see notifications; you are not notified for your own likes or comments on your own posts.

Never commit `.env`. Only `.env.example` is in git.

## Ports

| What | Port |
| --- | --- |
| Angular | 4200 |
| Gateway | 8080 |
| Auth / User / Post / Notifications | 8081–8084 |
| MongoDB | 27017 |
| Kafka (host) | 9092 |

## Notes

- JWT `sub` is the auth user id. Profiles store `authUserId`.
- Media is a URL (the UI may send a compressed data URL). There is no object store.
- Inbox polling starts after client hydration, not during SSR `ngOnInit`.

## Not production

Shared JWT secret, polling instead of push, data-URL photos, Compose Kafka. Fine for a portfolio clone; not a template to ship as-is.

## License

[MIT](LICENSE)
