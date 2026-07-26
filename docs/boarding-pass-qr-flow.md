# GetYourRide — Boarding Pass / QR Ticket Flow

This document describes how QR-based boarding passes are generated, displayed, and invalidated for shuttle bookings.

## Overview

When a student books a shuttle, a QR code is generated **once**, at booking time. Every subsequent time the student taps **"Show Ticket"**, the app re-renders the same `BoardingPassScreen` using the stored booking data — there is no PDF download and no re-generation of a new QR code per view. The QR code remains valid until the associated trip starts, at which point it is invalidated server-side.

---

## 1. QR Generation — Payload, Not Bitmap

The backend/app stores the **QR payload** (the string encoded into the QR), not a rendered image.

- Example payload: `ticketId|bookingId|signature` (a signed/HMAC token to prevent tampering).
- QR generation is deterministic — the same payload always produces the same QR pattern.
- The bitmap is generated **client-side, on demand**, from the stored payload:

```kotlin
val qrPayload = booking.qrToken
val qrBitmap = remember(qrPayload) { generateQrBitmap(qrPayload) }
```

This avoids persisting or transferring image files and means "Show Ticket" is just a data fetch, not a generation call.

---

## 2. "Show Ticket" Flow

1. Student taps **Show Ticket** from `My Rides`.
2. `TripBookingViewModel` requests the booking via `ShuttleRepository` — either from a local cache (Room) or `GET /api/bookings/{id}`.
3. `BoardingPassScreen` renders using the returned `BoardingPassDetails` (ticket ID, shuttle ID, plate number, driver, pickup/drop-off, departure time, status).
4. The QR bitmap is regenerated locally from the stored `qrToken` — no new backend call needed for the QR itself.

No PDF is created or downloaded at any point in this flow.

---

## 3. Expiry Model

The QR code itself does not expire — it's just an encoded string. What expires is its **validity**, enforced by the backend at scan time.

### Status field
Add a `status` column on `bookings` (and/or the parent `trips` table, for shared shuttles):

| Status | Meaning |
|---|---|
| `CONFIRMED` | Booking is active; QR is valid for boarding |
| `IN_PROGRESS` | Trip has started; QR is no longer valid |
| `COMPLETED` | Trip finished |
| `CANCELLED` | Booking cancelled |

### Validation at scan time
When a driver scans a student's QR, the backend validation endpoint checks:

```
booking.status == CONFIRMED
AND trip.status == CONFIRMED  // relevant for shared shuttles
```

If either check fails, the scan is rejected — even if the QR content decodes correctly.

### Shared shuttle nuance
For shared shuttles, expiry should be driven by the **trip's** status, not just the individual booking's. A status flip on the parent `trip` entity should cascade down to all bookings on that trip, since one shuttle departure invalidates boarding for everyone on it.

---

## 4. Real-Time Status Updates

Trip status changes are pushed to the client over the existing WebSocket/STOMP channel (already used for live tracking), rather than requiring a manual refresh.

On receiving a status update:
- `CONFIRMED → IN_PROGRESS`: `BoardingPassScreen` updates the status badge (e.g. "Confirmed" → "Trip Started") and visually disables the QR code to make clear it's no longer usable for boarding.

---

## 5. Summary of Responsibilities

| Layer | Responsibility |
|---|---|
| **Booking creation (backend)** | Generate and store signed `qrToken` once per booking |
| **Client (Compose)** | Generate QR bitmap from stored token on every screen view; render boarding pass |
| **Backend (scan endpoint)** | Validate `qrToken` signature + check `booking.status` / `trip.status` before accepting boarding |
| **WebSocket/STOMP** | Push trip status changes to connected clients in real time |
| **Client (ViewModel)** | React to status push; update UI to reflect expiry (badge, QR disabled state) |
