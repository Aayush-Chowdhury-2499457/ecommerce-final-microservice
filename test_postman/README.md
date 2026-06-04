# Postman Collections — E-Commerce Microservices

All API calls go **through the API gateway** at `http://localhost:8080`. The gateway forwards
each `/api/...` path unchanged to the right service (there is no path rewriting / StripPrefix),
so the URL you hit is just `{{base_url}}` + the service path.

## Files

```
postman/
├── environments/
│   └── ecommerce-local.postman_environment.json   # local only
└── collections/
    ├── auth-service.postman_collection.json
    ├── user-service.postman_collection.json
    ├── product-service.postman_collection.json
    ├── cart-service.postman_collection.json
    ├── order-service.postman_collection.json
    ├── review-service.postman_collection.json
    ├── payment-service.postman_collection.json
    └── dummy-payment-api.postman_collection.json   # direct :9090, NOT via gateway
```

## How to import

1. Postman → **Import** → select all files in `collections/` and the file in `environments/`.
2. Top-right environment dropdown → choose **ecommerce-local**.

## How auth works through the gateway (important)

The gateway's `ValidationFilter` enforces this:

- **`/api/auth/**` is public** — no token needed (register, login, validate).
- **Every other route requires `Authorization: Bearer <token>`.** The gateway validates the
  token against `auth-service`, then **injects `X-User-Id` and `X-User-Role`** before forwarding.
  → You **never** set `X-User-Id` / `X-User-Role` yourself; sending only the Bearer token is enough.

### Typical flow

1. **auth-service → Register** (creates a user) — optional if the user already exists.
2. **auth-service → Login** — a test script automatically saves the returned JWT into the
   `{{token}}` environment variable.
3. Run any request in the other collections. They inherit `Bearer {{token}}` at the collection
   level, so they just work.

## Prerequisites (must be running)

| Component | Port |
|---|---|
| Eureka server | 8761 |
| Config server | (provides config to all services) |
| **API gateway** | **8080** |
| auth, user, product, cart, order, review, payment services | 8081–8087 |
| dummy-payment-api (standalone) | 9090 |

Also ensure each service's MySQL `root` password in the config server matches your local MySQL,
or the services won't start.

## Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `base_url` | `http://localhost:8080` | API gateway — used by all routed collections |
| `dummy_payment_url` | `http://localhost:9090` | direct URL for dummy-payment-api only |
| `token` | *(empty)* | JWT, auto-filled by Login |
| `userId`, `productId`, `categoryId`, `addressId`, `cartItemId`, `orderId`, `reviewId`, `paymentId`, `shoppingCartId` | `1` | sample path IDs — edit as needed |
| `username` | `johndoe` | sample username for lookup |
| `email` | `john@example.com` | sample email for lookup |

## Why dummy-payment-api is different

The gateway only routes `/api/**`. `dummy-payment-api` exposes `/dummy-payment/process` and is
**not** a gateway route — it's an internal simulator called by `payment-service`. Its single
request therefore points at `{{dummy_payment_url}}` (`:9090`) directly. For a real payment flow,
use **payment-service → Initiate Payment** through the gateway instead.
