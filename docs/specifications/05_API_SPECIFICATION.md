# REST API Specification Document
## Dukan Bill Manager — Complete Endpoints, Payloads & Auth Contracts

---

### 1. Authentication & Security Flow

All private endpoints require a valid JSON Web Token (JWT) provided in an **`httpOnly` cookie (`dukan_token`)** for Web browsers or an `Authorization: Bearer <token>` header for Mobile Apps. Access tokens expire after **15 minutes**; clients call `POST /api/auth/refresh` using the rotating refresh token (7-day expiry) to obtain a new access token without forcing re-login.

```mermaid
sequenceDiagram
    participant App as Mobile App / Frontend
    participant Server as Express Server (/api/auth)
    participant Data as Data Store

    Note over App,Server: 1. Login Flow
    App->>Server: POST /api/auth/login { phone, password }
    Server->>Data: Look up user & verify bcrypt password
    Data-->>Server: User verified
    Server-->>App: 200 OK (Set-Cookie: dukan_token=JWT; HttpOnly) + { user, token, refreshToken }

    Note over App,Server: 2. Authenticated Request
    App->>Server: GET /api/data/state (Bearer <token> or Cookie)
    Server->>Server: auth.requireAuth middleware validates JWT
    Server->>Data: Read data/users/<userId>.json
    Data-->>Server: Return State
    Server-->>App: 200 OK { ok: true, state: {...} }

    Note over App,Server: 3. Token Refresh Flow
    App->>Server: POST /api/auth/refresh { refreshToken }
    Server->>Server: Verify & rotate refresh token
    Server-->>App: 200 OK { token: <newJWT>, refreshToken: <newRefreshToken> }
```

---

### 2. API Endpoints Catalog

#### 2.1 Authentication & Profile Routes (`/api/auth`)

##### `POST /api/auth/login`
Authenticates a user with 10-digit mobile number and password.
* **Rate Limit:** 10 requests per 5 minutes per IP.
* **Request Body:**
```json
{
  "phone": "9812345678",
  "password": "user1234"
}
```
* **Success Response (200 OK):**
```json
{
  "ok": true,
  "user": {
    "id": "usr-1723456789000-a1b2c",
    "phone": "9812345678",
    "name": "Rajesh Kumar",
    "email": "rajesh@example.com",
    "role": "user"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "ref-7890abc-12345"
}
```

##### `POST /api/auth/refresh`
Issues a new short-lived access token and rotates the refresh token.
* **Request Body:** `{ "refreshToken": "ref-7890abc-12345" }` (or via `dukan_refresh` cookie).
* **Response (200 OK):**
```json
{
  "ok": true,
  "token": "eyJhbGciOiJIUzI1NiIsIn...",
  "refreshToken": "ref-new-98765-xyz"
}
```

##### `GET /api/auth/me`
Retrieves currently logged-in user profile.
* **Auth:** Required (`requireAuth`).

##### `POST /api/auth/switch`
Switches active session to another previously authorized user ID.
* **Auth:** Required (`requireAuth`).
* **Request Body:** `{ "userId": "usr-1723456789000-xyz99" }`

##### `POST /api/auth/change-password`
User changes their own password with 3-tier requirement validation.
* **Auth:** Required (`requireAuth`).
* **Request Body:** `{ "currentPassword": "...", "newPassword": "..." }`

---

#### 2.2 State Data Synchronization Routes (`/api/data`)

##### `GET /api/data/state`
Fetches complete business state (bills, suppliers, payments, items, settings).
* **Auth:** Required (`requireAuth`).

##### `PATCH /api/data/state`
Performs partial update / merge to user's bills, suppliers, payments, or settings.
* **Auth:** Required (`requireAuth`).
* **Request Body:**
```json
{
  "bills": [...],
  "suppliers": [...],
  "payments": [...],
  "items": [...]
}
```

##### `POST /api/data/sync`
Batch synchronization endpoint for mobile offline queue.
* **Auth:** Required (`requireAuth`).
* **Request Body:**
```json
{
  "lastSyncTimestamp": 1723456789000,
  "mutations": [
    {
      "id": "mut-1",
      "entityType": "BILL",
      "operation": "CREATE",
      "payload": { "id": "bill-101", "supplierName": "Tata", "grandTotal": 5000 }
    }
  ]
}
```

---

#### 2.3 Smart Gemini AI OCR Ingestion Route (`/api/ocr`)

##### `POST /api/ocr/extract`
Extracts structured Indian GST invoice data from base64 image or PDF with automatic failover rotation across a pool of 5 Gemini API keys and cascading model fallbacks.

* **Auth:** Required (`requireAuth`).
* **Rate Limit:** 30 requests per minute per IP.
* **Request Body:**
```json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ...",
  "mimeType": "image/jpeg"
}
```
* **Success Response (200 OK):**
```json
{
  "ok": true,
  "data": {
    "bills": [
      {
        "invoiceNo": "INV-2026-042",
        "date": "2026-08-22",
        "sellerName": "Shree Ganesh Traders",
        "sellerPhone": "9876543210",
        "sellerGstin": "23AAAAA0000A1Z5",
        "sellerState": "Madhya Pradesh",
        "sellerCity": "Indore",
        "sellerAddress": "Shop 12, Siyaganj",
        "items": [
          {
            "name": "Tata Salt 1kg",
            "hsn": "2501",
            "qty": 50,
            "unit": "Packet",
            "rate": 24.00,
            "discount": 0.00,
            "taxableAmount": 1200.00,
            "cgstAmount": 30.00,
            "sgstAmount": 30.00,
            "igstAmount": 0.00,
            "total": 1260.00
          }
        ]
      }
    ]
  }
}
```

---

#### 2.4 File Upload Routes (`/api/uploads`)

##### `POST /api/uploads/receipt`
Uploads a payment receipt image (multipart form-data, field name `receipt`) and returns a stored file path.
* **Auth:** Required (`requireAuth`).
* **Limits:** 5 MB max file size; `image/jpeg`, `image/png`, `application/pdf`.
* **Success Response (200 OK):**
```json
{ "ok": true, "receiptUrl": "/uploads/receipts/pay-1723456789000.jpg" }
```

---

#### 2.5 Notification Routes (`/api/notifications`)

* `GET /api/notifications` — Returns active system notifications and unread counters.
* `POST /api/notifications/read` — `{ "all": true }` OR `{ "ids": ["notif-1"] }`.
* `DELETE /api/notifications/:id` — Hides notification permanently for the user.
* `DELETE /api/notifications` — Clears all notifications for the user.

---

#### 2.6 Admin Portal Routes (`/api/auth/admin` & `/api/admin/notifications`)

##### `POST /api/auth/admin/login`
One-time admin authentication. Compares submitted password against a `bcrypt` hash server-side.
* **Rate Limit:** 5 requests per 15 minutes per IP.
* **Request Body:** `{ "adminPassword": "..." }`
* **Response (200 OK):** `Set-Cookie: dukan_admin_token=<JWT>; HttpOnly; SameSite=Strict`

##### `GET /api/auth/admin/list-users`
List all registered users.
* **Auth:** Required (`requireAdminAuth`).

##### `POST /api/auth/admin/create-user`
Create new user.
* **Auth:** Required (`requireAdminAuth`).
* **Request Body:** `{ "phone": "...", "name": "...", "password": "..." }`

##### `POST /api/auth/admin/reset-password`
Force reset a user's password.
* **Auth:** Required (`requireAdminAuth`).
* **Request Body:** `{ "userId": "...", "newPassword": "..." }`

##### `DELETE /api/auth/admin/delete-user/:id`
Delete a user.
* **Auth:** Required (`requireAdminAuth`).

##### `POST /api/admin/notifications`
Broadcast a notification to all users.
* **Auth:** Required (`requireAdminAuth`).
* **Request Body:** `{ "title": "...", "body": "...", "type": "..." }`

##### `POST /api/auth/admin/change-password`
Update the admin master password.
* **Auth:** Required (`requireAdminAuth`).
* **Request Body:** `{ "currentPassword": "...", "newPassword": "..." }`
