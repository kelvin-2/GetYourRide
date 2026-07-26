# Ticket & QR Code Viewing — Architecture Documentation

## 1. Overview

This document describes how a rider views their booking ticket (with QR code) at any point after booking, up until the ride occurs. The core design decision: **the QR code image is never persisted or cached — it is deterministically regenerated on demand from stable ticket data that is fetched from the backend.**

This avoids two classes of bugs:
- A cached/stale QR code that no longer matches the current trip state.
- Any need to synchronize an image file across devices, app reinstalls, or backend updates.

## 2. Core Principle: QR Content Is Derived, Not Stored

The QR code encodes a payload built from two immutable, backend-issued identifiers:

```kotlin
fun buildQrPayload(booking: BookingConfirmation): String {
    return "GYR|ticket=${booking.ticketId}|shuttle=${booking.shuttleId}"
}
```

Because `ticketId` and `shuttleId` never change for a given booking, calling `buildQrPayload()` with the same booking data always produces the same string, and `generateQrCodeBitmap()` always produces the same bitmap. This means:

- **No image storage is required.** The bitmap is generated in-memory every time the screen is composed.
- **No cache invalidation logic is required.** There is nothing to go stale, because nothing is stored.
- **Correctness is guaranteed by construction** — the QR always reflects whatever the backend currently returns for that ticket.

## 3. Data Flow

```
MyRidesScreen (list of RideCard)
        │
        │  user taps "Show Ticket" on a SCHEDULED ride
        ▼
Navigation: booking_confirmation/{ticketId}
        │
        ▼
BookingConfirmationRoute (Composable)
        │
        │  viewModel.getBookingByTicketId(ticketId)
        ▼
TripBookingViewModel
        │
        │  Retrofit call → GET /trips/ticket/{ticketId}
        ▼
Spring Boot backend (TripServiceImpl)
        │
        │  looks up Trip + TripStop by ticketId
        ▼
Returns BookingConfirmation JSON
        │
        ▼
TripMapper (backend → UI model)
        │
        ▼
BookingConfirmationScreen
        │
        │  qrBitmap = remember(booking.ticketId) { generateQrCodeBitmap(buildQrPayload(booking)) }
        ▼
QR code rendered on screen
```

## 4. Entry Point: `MyRidesScreen`

Each `RideCard` already represents one booked trip. Add a `ticketId` field to the card's data model (it should already exist as part of the trip/booking data returned from the backend) and a tap action or explicit "Show Ticket" button:

```kotlin
RideCard(
    ride = ride,
    onShowTicket = { navController.navigate("booking_confirmation/${ride.ticketId}") }
)
```

**Visibility rule:** the "Show Ticket" action is only shown for rides where `status == SCHEDULED`. Once a trip's status changes to `COMPLETED` or `CANCELLED`, the action is hidden from the list. This keeps the ticket/QR feature scoped to "up until the ride happens," per the requirement.

## 5. Screen: `BookingConfirmationRoute`

A thin wrapper composable that turns a `ticketId` into a live `BookingConfirmation` object:

```kotlin
@Composable
fun BookingConfirmationRoute(
    ticketId: String,
    viewModel: TripBookingViewModel = viewModel(factory = TripBookingViewModel.Factory)
) {
    val booking by viewModel.getBookingByTicketId(ticketId).collectAsState(initial = null)

    booking?.let {
        BookingConfirmationScreen(
            booking = it,
            onViewMyRides = { /* nav */ },
            onDownloadTicket = { /* not required per current scope */ },
            onContactSupport = { /* nav */ },
            onNavigate = { /* nav */ }
        )
    } ?: LoadingSpinner()
}
```

This is the same `BookingConfirmationScreen` already used right after a fresh booking — it is reused as a general-purpose "ticket viewer," not a one-time post-booking confirmation.

## 6. Backend: Ticket Lookup Endpoint

A new (or reused) read endpoint on the Spring Boot side:

```
GET /trips/ticket/{ticketId}
```

Responsibilities:
- Look up the `Trip` / `TripStop` by `ticketId`.
- Return the same shape of data currently produced for a fresh booking (so `TripMapper` doesn't need a second code path).
- Return `404` (or `410 Gone`) if the ticket no longer corresponds to an active/scheduled trip — e.g. after completion or cancellation — so the client can gracefully redirect back to `MyRidesScreen` instead of showing a QR for a trip that's no longer valid.

## 7. ViewModel Layer

`TripBookingViewModel` gains one additional method alongside the existing booking-creation logic:

```kotlin
fun getBookingByTicketId(ticketId: String): Flow<BookingConfirmation?> = flow {
    emit(null) // initial loading state
    val result = tripApi.getTicket(ticketId) // Retrofit call
    emit(TripMapper.toBookingConfirmation(result))
}
```

This keeps the pattern consistent with the existing standalone factory pattern already used for `TripBookingViewModel`.

## 8. QR Generation (unchanged from existing implementation)

```kotlin
val qrBitmap = remember(booking.ticketId) {
    generateQrCodeBitmap(buildQrPayload(booking))
}
```

Keying `remember` on `booking.ticketId` ensures the bitmap is only regenerated when the underlying ticket actually changes (e.g. navigating between two different tickets), not on every recomposition.

## 9. Ticket Lifecycle Summary

| Trip Status | "Show Ticket" visible on MyRidesScreen? | Ticket endpoint response |
|---|---|---|
| `SCHEDULED` | Yes | 200 + booking data (QR shown) |
| `COMPLETED` | No | 404 / 410 |
| `CANCELLED` | No | 404 / 410 |

This ensures the QR/ticket is only ever reachable "up until the ride happens," matching the intended scope, without needing any client-side expiry logic, timers, or stored state.

## 10. Why Not Persist the QR Image or Cache It Locally?

- **Single source of truth:** the backend trip/ticket record is authoritative. Any local cache (Room DB, file storage, SharedPreferences) introduces a second copy of truth that can drift if the backend state changes (e.g. driver/vehicle reassignment, cancellation).
- **Deterministic regeneration is cheap:** QR generation via ZXing is fast and lightweight; there's no performance reason to cache the bitmap across app sessions.
- **Simplicity:** no cache invalidation, no storage permissions, no cleanup logic when a ride completes.

If offline viewing is required in the future (e.g. no network at pickup time), the recommended extension is to cache the **booking data** (not the bitmap) locally via Room, keyed by `ticketId`, with a simple "last synced" fallback — the QR bitmap would still be regenerated client-side from that cached data, preserving the same "derived, not stored" principle.
