# Manual Postman Testing Guide — E-Commerce Microservices

Everything goes through the **API gateway** at `http://localhost:8080` (except `dummy-payment-api`,
which is direct on `:9090`). The gateway forwards `/api/...` paths unchanged to each service.

---

## 1. Headers — what to set, and when

There are only three headers you ever need to add manually:

| Header | Value | When to add it |
|---|---|---|
| `Authorization` | `Bearer <token>` | On **every** request **except** `POST /api/auth/register` and `POST /api/auth/login`. Paste the token returned by Login. |
| `Content-Type` | `application/json` | On every request that has a **body** (all POST / PUT / PATCH). Not needed for GET / DELETE. |
| `Accept` | `application/json` | Optional. Postman sends `*/*` by default, which works fine. Add only if you want to be explicit. |

**Do NOT set** `X-User-Id` or `X-User-Role`.
The gateway's `ValidationFilter` validates your JWT, calls auth-service, and **injects those two
headers itself** before forwarding. If you set them manually they are ignored/overwritten.

In Postman:
- **Body** → choose **raw** → select **JSON** from the dropdown on the right (this auto-sets `Content-Type`).
- **Authorization** tab → Type **Bearer Token** → paste token (or add the header manually under the Headers tab).

---

## 2. Other fields you may need

| Field | Where in Postman | Notes |
|---|---|---|
| **Path variables** (e.g. `1` in `/api/users/1`) | directly in the URL | replace the sample `1` with a real id from a prior GET/POST response |
| **Body mode** | Body tab | always **raw + JSON** for the bodies below |
| **Query params** | Params tab | none of these endpoints use query params |
| **Bearer token** | Authorization tab | obtained from Login response field `token` |

---

## 3. The login flow (do this first)

1. `POST /api/auth/register` — create a user (skip if it already exists).
2. `POST /api/auth/login` — response body is `{ "token": "eyJhbGciOi..." }`.
3. Copy that `token` value.
4. For every other request: **Authorization** tab → **Bearer Token** → paste it.

> Tip: a CUSTOMER token can use cart/order/review/payment customer endpoints.
> Admin-only endpoints (marked **ADMIN**) need a token whose user has role `ADMIN`.

---

## 4. Endpoint reference

Prepend `http://localhost:8080` to every path unless stated otherwise.
"Headers" column lists what to add **on top of** the global rules in section 1.

### auth-service  (PUBLIC — no token)

| # | Method | Path | Headers | Body (raw JSON) | Success |
|---|---|---|---|---|---|
| 1 | POST | `/api/auth/register` | `Content-Type: application/json` | `{ "name":"John Doe", "username":"johndoe", "email":"john@example.com", "password":"secret123", "phoneNumber":"9876543210", "dateOfBirth":"1995-05-20" }` | 201 Created |
| 2 | POST | `/api/auth/login` | `Content-Type: application/json` | `{ "usernameOrEmail":"johndoe", "password":"secret123" }` | 200 OK → `{ token }` |
| 3 | POST | `/api/auth/validate` | `Authorization: Bearer <token>` | *(none)* | 200 OK → claims |

Validation: username 5–50 chars, password ≥ 6, valid email, phoneNumber exactly 10 digits, dateOfBirth in the past.

---

### user-service

| # | Method | Path | Headers | Body (raw JSON) | Role | Success |
|---|---|---|---|---|---|---|
| 1 | POST | `/api/users` | Auth + `Content-Type` | `{ "name":"Jane Doe", "username":"janedoe", "email":"jane@example.com", "phoneNumber":"9876543211", "dateOfBirth":"1996-03-15" }` | ADMIN | 201 |
| 2 | GET | `/api/users` | Auth | — | ADMIN | 200 |
| 3 | GET | `/api/users/1` | Auth | — | self-or-admin | 200 |
| 4 | GET | `/api/users/username/johndoe` | Auth | — | any | 200 |
| 5 | GET | `/api/users/email/john@example.com` | Auth | — | any | 200 |
| 6 | PUT | `/api/users/1` | Auth + `Content-Type` | `{ "name":"John Updated", "email":"john.updated@example.com", "phoneNumber":"9876500000", "dateOfBirth":"1995-05-20" }` | self-or-admin | 200 |
| 7 | DELETE | `/api/users/1` | Auth | — | ADMIN | 204 |

**Addresses** (`{userId}` = `1`)

| # | Method | Path | Headers | Body (raw JSON) | Role | Success |
|---|---|---|---|---|---|---|
| 8 | POST | `/api/users/1/addresses` | Auth + `Content-Type` | `{ "houseNo":"12A", "area":"MG Road", "city":"Bangalore", "state":"Karnataka", "country":"India", "pincode":"560001" }` | self-or-admin | 201 |
| 9 | GET | `/api/users/1/addresses` | Auth | — | self-or-admin | 200 |
| 10 | GET | `/api/users/1/addresses/1` | Auth | — | self-or-admin | 200 |
| 11 | PUT | `/api/users/1/addresses/1` | Auth + `Content-Type` | `{ "houseNo":"99B", "area":"Indiranagar", "city":"Bangalore", "state":"Karnataka", "country":"India", "pincode":"560038" }` | self-or-admin | 200 |
| 12 | DELETE | `/api/users/1/addresses/1` | Auth | — | self-or-admin | 204 |

Validation: city/state/country/pincode required; pincode 4–10 digits; phoneNumber 10 digits.

---

### product-service

**Products**

| # | Method | Path | Headers | Body (raw JSON) | Role | Success |
|---|---|---|---|---|---|---|
| 1 | POST | `/api/products` | Auth + `Content-Type` | `{ "productName":"Laptop", "description":"14-inch ultrabook", "price":74999.0, "stock":50, "categoryId":1, "imageUrl":"https://example.com/laptop.png" }` | ADMIN | 201 |
| 2 | GET | `/api/products` | Auth | — | any | 200 |
| 3 | GET | `/api/products/1` | Auth | — | any | 200 |
| 4 | GET | `/api/products/productName/Laptop` | Auth | — | any | 200 |
| 5 | GET | `/api/products/category/1` | Auth | — | any | 200 |
| 6 | PUT | `/api/products/1` | Auth + `Content-Type` | `{ "productName":"Laptop Pro", "description":"16-inch", "price":99999.0, "stock":40, "categoryId":1, "imageUrl":"https://example.com/pro.png" }` | ADMIN | 200 |
| 7 | PATCH | `/api/products/1/stock` | Auth + `Content-Type` | `{ "stock":100 }` | ADMIN | 200 |
| 8 | PUT | `/api/products/1/reduce-stock` | Auth + `Content-Type` | `{ "quantity":5 }` | ADMIN-if-present | 200 |
| 9 | PUT | `/api/products/1/restock` | Auth + `Content-Type` | `{ "quantity":20 }` | ADMIN-if-present | 200 |
| 10 | DELETE | `/api/products/1` | Auth | — | ADMIN | 204 |

**Categories**

| # | Method | Path | Headers | Body (raw JSON) | Role | Success |
|---|---|---|---|---|---|---|
| 11 | POST | `/api/categories` | Auth + `Content-Type` | `{ "categoryName":"Electronics" }` | ADMIN | 201 |
| 12 | GET | `/api/categories` | Auth | — | any | 200 |
| 13 | GET | `/api/categories/1` | Auth | — | any | 200 |
| 14 | GET | `/api/categories/categoryName/Electronics` | Auth | — | any | 200 |
| 15 | PUT | `/api/categories/1` | Auth + `Content-Type` | `{ "categoryName":"Consumer Electronics" }` | ADMIN | 200 |
| 16 | DELETE | `/api/categories/1` | Auth | — | ADMIN | 204 |

Validation: productName 2–255 chars, price > 0, stock ≥ 0, categoryId required; categoryName 2–100 chars.

---

### cart-service  (CUSTOMER)

| # | Method | Path | Headers | Body (raw JSON) | Success |
|---|---|---|---|---|---|
| 1 | GET | `/api/carts` | Auth | — | 200 |
| 2 | POST | `/api/carts/items` | Auth + `Content-Type` | `{ "productId":1, "quantity":2 }` | 201 |
| 3 | PATCH | `/api/carts/items` | Auth + `Content-Type` | `{ "productId":1, "quantity":3 }` | 200 |
| 4 | DELETE | `/api/carts/items/1` | Auth | — | 200 |
| 5 | DELETE | `/api/carts/clear` | Auth | — | 204 |
| 6 | POST | `/api/carts/checkout` | Auth + `Content-Type` | `{ "addressId":1 }` | 200 |

The cart is resolved from the token's user — no userId needed in the URL. quantity ≥ 1.

---

### order-service

| # | Method | Path | Headers | Body (raw JSON) | Role | Success |
|---|---|---|---|---|---|---|
| 1 | POST | `/api/orders` | Auth + `Content-Type` | `{ "userId":1, "shoppingCartId":1, "addressId":1 }` | any | 201 |
| 2 | GET | `/api/orders` | Auth | — | ADMIN | 200 |
| 3 | GET | `/api/orders/users/1` | Auth | — | self-or-admin | 200 |
| 4 | GET | `/api/orders/1` | Auth | — | self-or-admin | 200 |
| 5 | GET | `/api/orders/users/1/products/1/has-purchased` | Auth | — | any | 200 → `true`/`false` |
| 6 | PATCH | `/api/orders/1/status` | Auth + `Content-Type` | `{ "orderStatus":"CONFIRMED" }` | ADMIN | 200 |
| 7 | PUT | `/api/orders/1/payment-status` | Auth + `Content-Type` | `{ "paymentStatus":"PAID" }` | any | 200 |
| 8 | PATCH | `/api/orders/1/cancel` | Auth | — | self-or-admin | 200 |

`orderStatus`: `PLACED` | `CONFIRMED` | `SHIPPED` | `DELIVERED` | `CANCELLED`
`paymentStatus`: `PENDING` | `PAID` | `FAILED` | `REFUNDED`

---

### review-service

| # | Method | Path | Headers | Body (raw JSON) | Role | Success |
|---|---|---|---|---|---|---|
| 1 | POST | `/api/reviews` | Auth + `Content-Type` | `{ "productId":1, "orderId":1, "rating":5, "description":"Excellent product." }` | CUSTOMER | 201 |
| 2 | GET | `/api/reviews` | Auth | — | any | 200 |
| 3 | GET | `/api/reviews/1` | Auth | — | any | 200 |
| 4 | GET | `/api/reviews/users/1` | Auth | — | any | 200 |
| 5 | GET | `/api/reviews/product/1` | Auth | — | any | 200 |
| 6 | GET | `/api/reviews/product/1/average` | Auth | — | any | 200 → number |
| 7 | PUT | `/api/reviews/1` | Auth + `Content-Type` | `{ "rating":4, "description":"Good, battery could be better." }` | owner | 200 |
| 8 | DELETE | `/api/reviews/1` | Auth | — | owner-or-admin | 204 |

`rating` must be 1–5. description ≤ 1000 chars.

---

### payment-service

| # | Method | Path | Headers | Body (raw JSON) | Role | Success |
|---|---|---|---|---|---|---|
| 1 | POST | `/api/payments/initiate` | Auth + `Content-Type` | `{ "orderId":1, "amount":74999.0 }` | CUSTOMER | 200/201 |
| 2 | GET | `/api/payments` | Auth | — | ADMIN | 200 |
| 3 | GET | `/api/payments/1` | Auth | — | owner-or-admin | 200 |
| 4 | GET | `/api/payments/orders/1` | Auth | — | owner-or-admin | 200 |

amount must be > 0.

---

### dummy-payment-api  ⚠️ NOT via gateway — direct, no token

| # | Method | Full URL | Headers | Body (raw JSON) | Success |
|---|---|---|---|---|---|
| 1 | POST | `http://localhost:9090/dummy-payment/process` | `Content-Type: application/json` | `{ "orderId":1, "amount":74999.0, "currency":"INR" }` | 200 |

This is an internal simulator called by payment-service. The gateway does **not** route it, so use
the `:9090` URL directly and send **no** `Authorization` header. For a real flow, use
**payment-service → Initiate Payment** through the gateway instead.

---

## 5. Common errors while testing

| Status | Likely cause |
|---|---|
| 401 Unauthorized | Missing/expired `Authorization: Bearer <token>` on a non-auth endpoint. Re-run Login. |
| 403 Forbidden | Your token's role isn't allowed (e.g. CUSTOMER hitting an ADMIN endpoint). |
| 503 Service Unavailable | Gateway couldn't reach auth-service to validate the token (auth-service down). |
| 400 Bad Request | Body failed validation (see the per-service validation notes above). |
| Connection refused | Target service / gateway not running, or DB login failed at startup (check MySQL root password in config server). |

## 6. Prerequisites running

Eureka (8761) · Config server · **API gateway (8080)** · auth (8081) · user (8082) ·
product (8083) · cart (8084) · order (8085) · review (8086) · payment (8087) ·
dummy-payment-api (9090).
