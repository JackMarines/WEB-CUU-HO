# AGENTS.md — Emergency Response Platform

## Repo structure

```
WEB-CUU-HO/
├── emergency-response-backend/   # Spring Boot 3.3.1 + Maven + MySQL
└── emergency-response-frontend/  # Next.js 16.2.7 + Tailwind v4 + Turbopack
```

## Commands

```
# Backend
cd emergency-response-backend && mvn compile          # rebuild (run manually)
# Frontend
cd emergency-response-frontend && npm run dev          # dev server (Turbopack)
npx tsc --noEmit                                       # typecheck
npm run lint                                            # lint
```

## Seed accounts

| Email | Password | Role |
|---|---|---|
| `admin@emergency.vn` | `admin123` | admin |
| `superadmin@emergency.vn` | `admin123` | superadmin |

## Critical: `spring.jpa.open-in-view=false`

Any service that lazy-loads entity relations via `fromEntity()` **must** have `@Transactional(readOnly = true)` at class level (or `@Transactional` for write methods). Without it, `FetchType.LAZY` access after the session closes → `LazyInitializationException` (500).

Already fixed: `CallService`, `ResponseService`, `AiService`.

## No Lombok

- DTOs → Java records
- Entities → manual getters/setters
- `@Autowired` field injection (existing convention)

## Database

- MySQL `localhost:3306`, user `root`, password `truc1612`
- DB: `emergency_response` (auto-created)
- `spring.jpa.hibernate.ddl-auto=update` + `schema.sql` init
- Offline CLI: `"C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe" -u root -p`

## API security

| Pattern | Access |
|---|---|
| `GET /api/calls/**` | permitAll |
| `POST /api/calls/**` | authenticated |
| `PUT /api/calls/**` | hasRole("admin") |
| `/api/admin/**` | hasAnyRole("admin", "superadmin") |
| `/api/auth/**` | permitAll |
| `/api/chat/**` | authenticated |

## Frontend conventions

- Leaflet components must be `dynamic(() => import(...), { ssr: false })` — no SSR
- Map tiles: CartoDB dark (`https://{s}.basemaps.cartocdn.com/dark_all/...`)
- Address geocoding: Nominatim OSM (free, no API key) — `User-Agent: EmergencyResponse/1.0` header required
- AI chat: rule-based (no OpenAI key, no Spring AI)
- Design: coral `#f86e64`, DM Sans font, Tailwind v4 classes: `rounded-section`, `rounded-pill`, `rounded-tag`, `bg-surface-*`, `border-border-default`, `text-text-*`
- `ProtectedRoute` in `admin/layout.tsx` accepts `['admin', 'superadmin']`

## Roles

- `superadmin` > `admin` > `user`
- Superadmin manages admins + users, cannot demote self
- Admin manages regular users only
- Auth: JWT + OAuth2 (Google/GitHub — placeholder clients)
