# Shopkeeper Login Portal — Spring Boot App

A complete backend covering everything you listed:

- **Login portal** — Shopkeeper registration/login with JWT auth (Spring Security)
- **Customer details** — CRUD + search
- **Bill details** — item × quantity = total, computed server-side, auto stock deduction
- **Notifications** — SMS + WhatsApp via Twilio, sent on bill creation, payment, and khata reminders
- **Bill payment** — UPI or Cash, with sender & receiver details recorded
- **Barcode** — Code128 barcode generated per payment (scan at counter to pull up the bill)
- **Khata book / Udhaar khata** — credit ledger per customer, due-date tracking, automatic pending-balance reminders (daily scheduled job)
- **Stock management** — products, quantities, low-stock threshold alerts

## Tech stack
Java 17 · Spring Boot 3.3 · Spring Security + JWT · Spring Data JPA · H2 (swap to MySQL) · ZXing (barcode) · Twilio (SMS/WhatsApp)

## 1. Run it

```bash
cd shopkeeper-app
mvn spring-boot:run
```

App starts on `http://localhost:8080`. H2 console at `/h2-console` (JDBC URL: `jdbc:h2:mem:shopkeeperdb`).

To use MySQL instead of the default in-memory H2, uncomment the MySQL block in
`src/main/resources/application.properties` and comment out the H2 lines.

## 2. Configure notifications & barcode storage

In `application.properties`:

```properties
twilio.account-sid=YOUR_TWILIO_ACCOUNT_SID
twilio.auth-token=YOUR_TWILIO_AUTH_TOKEN
twilio.sms-from-number=+1XXXXXXXXXX
twilio.whatsapp-from-number=whatsapp:+14155238886
```

Without real Twilio credentials, the app runs in **stub mode** — messages are logged
to the console instead of actually sent, so you can develop/test without an account.

Barcodes are saved as PNGs under `./barcodes` (configurable via `app.barcode.storage-path`).

## 3. API walkthrough

### Auth — registration (3-step, OTP-verified)
```
POST /api/auth/register/initiate
{ "name": "Ramesh Kumar", "companyName": "Ramesh General Store", "mobileNumber": "+919876500000" }
```
Validates the request, checks the mobile number isn't already registered, generates a secure
6-digit OTP (5 min expiry), and sends it via SMS + WhatsApp.

```
POST /api/auth/register/verify-otp
{ "mobileNumber": "+919876500000", "otp": "482913" }
```
Validates the OTP (expiry + max 5 attempts), marks the phone verified, and returns a short-lived
**registration token** (10 min).

```
POST /api/auth/register/complete
{ "registrationToken": "...", "username": "rameshstore", "password": "Str0ng!Pass", "confirmPassword": "Str0ng!Pass" }
```
Validates the token, enforces the password policy (8+ chars, upper/lower/digit/special), ensures
the username is unique, BCrypt-hashes the password, creates the user as `ACTIVE`, and returns a
JWT **access token** (15 min) + **refresh token** (7 days).

### Auth — login
```
POST /api/auth/login   { "username": "rameshstore", "password": "Str0ng!Pass" }
POST /api/auth/refresh-token   { "refreshToken": "..." }
```
Use the access token as `Authorization: Bearer <token>` on every other request.

### Auth — forgot password
```
POST /api/auth/forgot-password           { "mobileNumber": "+919876500000" }
POST /api/auth/forgot-password/verify    { "mobileNumber": "+919876500000", "otp": "482913" }
                                          -> returns { resetToken }
POST /api/auth/reset-password            { "resetToken": "...", "newPassword": "...", "confirmPassword": "..." }
```

### Auth — resend OTP
```
POST /api/auth/resend-otp   { "mobileNumber": "+919876500000", "purpose": "REGISTER" }
```
Enforces a 60-second cooldown since the last OTP and invalidates the previous one.

Every `/api/auth/**` response is wrapped in the standard envelope:
```json
{ "success": true,  "message": "...", "data": { ... } }
{ "success": false, "message": "...", "errors": [ "..." ] }
```

Interactive API docs (Swagger UI, publicly accessible) are at `/swagger-ui.html` once the app is running.

### Customers
```
POST   /api/customers
GET    /api/customers?search=name
GET    /api/customers/{id}
PUT    /api/customers/{id}
DELETE /api/customers/{id}
```

### Stock management (products)
```
POST   /api/products              { name, sku, pricePerUnit, unit, stockQuantity, lowStockThreshold }
GET    /api/products
GET    /api/products/low-stock
PATCH  /api/products/{id}/adjust-stock   { "delta": -3 }   // restock with positive delta
DELETE /api/products/{id}
```

### Billing (item × number = total)
```
POST /api/bills
{
  "customerId": 1,
  "items": [ { "productId": 5, "quantity": 3 }, { "productId": 7, "quantity": 1 } ],
  "notifyCustomer": true
}
```
Each line's total = `pricePerUnit × quantity`; the bill total is the sum. Stock is
automatically decremented. If `notifyCustomer` is true, an SMS + WhatsApp summary is sent.

### Payments (UPI / Cash + barcode)
```
POST /api/payments
{
  "billId": 10,
  "mode": "UPI",                 // or "CASH"
  "amountPaid": 250.00,
  "senderName": "Ramesh", "senderUpiId": "ramesh@upi", "senderPhone": "9876500000",
  "receiverName": "My Shop", "receiverUpiId": "myshop@upi",
  "transactionRef": "TXN123456",
  "addRemainderToKhata": true,
  "khataDueDate": "2026-08-01"
}
```
If `amountPaid` is less than the bill total, the remainder is automatically pushed
into the udhaar khata book (when `addRemainderToKhata` is true). A barcode PNG is
generated for every payment:
```
GET /api/payments/bill/{billId}/barcode   -> image/png
```

### Khata book (udhaar khata)
```
POST /api/khata                 { customerId, type: "YOU_GAVE"|"YOU_GOT", amount, note, dueDate }
GET  /api/khata
GET  /api/khata/pending
GET  /api/khata/customer/{id}
POST /api/khata/{id}/settle     { "amount": 100.00 }
```
A daily scheduled job (`app.khata.reminder-cron`, default 9 AM) scans for entries
whose `dueDate` has arrived and sends a pending-balance reminder via SMS + WhatsApp,
then marks the entry so it isn't re-sent every day.

## 4. Project layout

```
src/main/java/com/shopkeeper/app/
  config/       JWT util, Spring Security config, Swagger/OpenAPI config
  security/     JWT filter, UserDetailsService, REST auth entry point
  entity/       User, Otp, RegistrationToken, PasswordResetToken,
                Customer, Product, Bill, BillItem, Payment, KhataEntry
  repository/   Spring Data JPA repositories
  dto/          Request/response payloads + ApiResponse envelope
  validation/   Custom Bean Validation annotations (mobile number, password policy, username)
  mapper/       Entity -> safe response DTO mapping
  util/         OTP + secure token generators
  service/      Business logic (auth/OTP flow, billing math, notifications, barcode, khata, scheduler)
  controller/   REST endpoints
  exception/    Custom exceptions + centralized GlobalExceptionHandler
```

## 5. Notes / next steps
- Every entity is scoped to the logged-in shopkeeper (multi-tenant ready) — one deployment can serve many shops.
- Swap H2 for MySQL/Postgres for production (see commented block in `application.properties`).
- Twilio handles both SMS and WhatsApp; for WhatsApp you'll need a Meta-approved sender number.
- The barcode encodes `BILL:<number>;AMT:<amount>;MODE:<UPI|CASH>` — pair it with a scanner app/POS to look up bills instantly.
- OTPs are never stored or logged in plaintext — only a BCrypt hash is persisted, and log lines omit the value.
- Passwords, OTPs, and JWTs are never written to logs; only high-level events (registration, login, password reset, auth failures) are logged via SLF4J.
- The registration flow's Step 1 → Step 2 handoff (name/company) is bridged by an in-memory cache (`PendingRegistrationCache`) since the OTP-verify call only carries mobile number + OTP. For a multi-instance deployment behind a load balancer, swap this for a shared store (Redis) so Step 1 and Step 2 don't need to hit the same instance.
- Refresh tokens are stateless JWTs here (not persisted), so they can't be revoked individually before expiry. Add a persisted refresh-token table/blocklist if you need server-side revocation (e.g. on logout).
# shopkeeper
# shopkeeper-backend
# shopkeeper-backend
