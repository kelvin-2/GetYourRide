# Tasks — GetYourRide Frontend Build Plan (Tracking UI)

This document covers **frontend/Android-only** work for the live tracking screen. Owned and built separately from the backend — see [`tasks.md`](tasks.md) for backend scope.

**Audit note:** before writing this plan, the existing repo (`kelvin-2/GetYourRide`) was checked. The tracking screen is **not a blank slate** — most of the scaffolding already exists. Every phase below says explicitly whether it's modifying an existing file or creating a genuinely new one, so nothing gets duplicated.

## What already exists (confirmed by reading the code)

| Piece | File | Status |
|---|---|---|
| Map screen (Compose) | `ui/screens/Tracking/TrackingScreen.kt` | ✅ Exists — OSMDroid `MapView`, driver + destination markers, single polyline, FAB, bottom info card |
| ViewModel | `viewmodel/TrackingViewModel.kt` | ✅ Exists — `StateFlow<TrackingUiState>`, `startTracking()`, `cancelRide()` |
| Socket contract | `RideLocationSocket` interface (in `TrackingViewModel.kt`) | ✅ Exists — clean `connect()`/`disconnect()` contract, already written to be swappable |
| Mock socket | `viewmodel/MockRideLocationSocket.kt` | ✅ Exists — simulates random driver movement every 2s, already wired into `MainActivity` |
| Trip info model | `data/model/TripTrackingInfo.kt` | ✅ Exists — driver, vehicle, ETA, `RideStatus` enum |
| Bottom info card | `DriverInfoCard` composable (in `TrackingScreen.kt`) | ✅ Exists — driver, vehicle, message/call/cancel buttons |
| REST client | `data/remote/api/TripApi.kt` | ✅ Exists — Retrofit interface with several trip endpoints, but **no single-trip-by-id or stops endpoint yet** |
| Real STOMP socket implementation | — | ❌ Does not exist — only the interface + mock |
| Multi-stop / leg-based route rendering | — | ❌ Does not exist — current polyline is a single straight line, driver → one destination |
| Marker animation/interpolation | — | ❌ Does not exist — `driverMarker.position` is set directly, no tween |
| Custom map tiles/markers | — | ❌ Uses default `TileSourceFactory.MAPNIK` and default OSMDroid pins |
| Stop-state markers (passed/upcoming/current) | — | ❌ Does not exist — only driver + single destination marker |

**Known bug spotted in passing (not tracking-specific):** `TripApi.searchTrips()` sends `pickupLat`/`pickupLng`/`destinationLat`/`destinationLng` as query params, but the backend's `/api/trips/search` expects `depLat`/`depLng`/`destLat`/`destLng`. Worth a one-line fix whenever that endpoint gets touched.

---

## Phase A — Map & Marker Visual Polish (modify existing)

**Files touched:** `TrackingScreen.kt` (`OsmMapView` composable only)

**Objective:** Improve the existing map's look without rebuilding it.

**Deliverables:**
- Swap `TileSourceFactory.MAPNIK` for a CartoDB Positron tile source in the existing `MapView.apply {}` block
- Replace the default `Marker` icons (driver + destination) with custom drawables via `marker.icon = ...`
- Keep the existing `Polyline`/`DisposableEffect`/`LaunchedEffect` structure — just restyle what's already there

**What to check first:**
- [ ] Confirm `OsmMapView`'s current `remember { MapView(context).apply {...} }` block — this is the only place the tile source needs to change
- [ ] Confirm marker drawables don't already exist somewhere unused in `res/drawable`

**Acceptance Criteria:**
- [ ] Map renders CartoDB Positron tiles instead of default Mapnik
- [ ] Driver and destination markers use custom icons, not default pins
- [ ] No existing functionality (recenter FAB, destination chip) regresses

---

## Phase B — Marker Animation + Multi-Stop Route (modify existing, extend data model)

**Files touched:** `TrackingScreen.kt` (`OsmMapView`), `TrackingViewModel.kt` (`TrackingUiState`)

**Objective:** Smooth marker movement, and extend the single-destination model to handle an ordered list of stops once the backend delivers leg data.

**Deliverables:**
- Replace the direct `driverMarker.position = point` assignment in the `LaunchedEffect` with an interpolated animation (`Animatable`/coroutine tween) between old and new position
- Extend `TrackingUiState` to carry a `stops: List<GeoPoint>` (or similar) alongside the existing `driverLocation`/`destinationLocation`, without breaking the existing single-destination fields
- Split the current single `Polyline` into two: solid traveled segment + dashed remaining segment
- Add small marker overlays for intermediate stops with 3 visual states (passed/upcoming/current) — this part is new, since only driver + destination markers exist today

**What to check first:**
- [ ] Confirm whether `TrackingUiState` is used anywhere else in the codebase before changing its shape (search for other consumers)
- [ ] Test animation smoothness against `MockRideLocationSocket`'s existing 2-second update interval before touching real data

**Acceptance Criteria:**
- [ ] Marker moves smoothly between updates against the mock socket, no visible snapping
- [ ] Traveled/remaining polyline split renders correctly with sample multi-stop data
- [ ] Stop markers reflect passed/upcoming/current state correctly
- [ ] Existing single-destination behavior still works as a fallback if only one destination is provided (don't break current callers)

---

## Phase C — Real STOMP Socket Implementation (new file, existing interface)

**Files touched:** new `viewmodel/StompRideLocationSocket.kt`, `MainActivity.kt` (swap which implementation gets injected)

**Objective:** Implement the already-defined `RideLocationSocket` interface against a real STOMP client — this is the one genuinely new file needed, since the contract and mock already exist.

**Dependency:** requires backend Phase 3 (`step-4-websocket`) to be live — `/topic/trip/{tripId}` must exist and be publishing `LOCATION_UPDATE`/`STOP_EVENT` messages.

**Deliverables:**
- `StompRideLocationSocket : RideLocationSocket` — connects to the backend's STOMP endpoint, maps incoming `LOCATION_UPDATE` messages to the existing `DriverLocationUpdate` data class
- Handle `STOP_EVENT` messages too — likely needs a small addition to the `RideLocationSocket` interface's `onUpdate` callback shape, or a second callback, to avoid overloading `DriverLocationUpdate` with data it wasn't designed for
- In `MainActivity.kt`, swap `remember { MockRideLocationSocket() }` for the real implementation behind a build flag or environment check, so the mock stays available for UI development/demos

**What to check first:**
- [ ] Confirm which STOMP client library is already a dependency (check `build.gradle.kts`) before adding a new one
- [ ] Test the real socket against a manually-triggered backend test message before wiring it into the full trip flow

**Acceptance Criteria:**
- [ ] Real driver location updates flow into `TrackingViewModel` exactly like the mock did — no changes needed downstream in `TrackingScreen.kt` if the interface is respected
- [ ] `STOP_EVENT` messages update stop marker state from Phase B
- [ ] Mock socket remains intact and swappable back in for demos/previews

---

## Phase D — Real Trip Data (modify existing)

**Files touched:** `TrackingViewModel.kt` (`startTracking()`), `TripApi.kt` (add endpoints)

**Objective:** Replace the hardcoded `TripTrackingInfo` in `startTracking()` (currently marked with a `TODO`) with real data.

**Deliverables:**
- Add `getTripById(tripId)` and `getTripStops(tripId)` to the existing `TripApi.kt` interface — small addition, not a new file
- `startTracking()` calls these instead of building a hardcoded `TripTrackingInfo`
- Fix the `searchTrips` param name mismatch (`pickupLat` → `depLat` etc.) noted in the audit above, while in this file anyway

**What to check first:**
- [ ] Confirm the exact `TripResponse` shape returned by the backend (already documented in `GetYourRide_Tracking_Documentation.md`) maps cleanly onto the existing `TripTrackingInfo` fields, or note the gaps

**Acceptance Criteria:**
- [ ] `startTracking()` shows real driver/vehicle/ETA data for a given trip ID, no hardcoded values remain
- [ ] `searchTrips` sends correctly-named params matching the backend

---

## Phase E — Resilience + Polish (modify existing)

**Files touched:** `TrackingScreen.kt`, `TrackingViewModel.kt`

**Objective:** The `isConnected`/`error` fields already exist in `TrackingUiState` and there's already a "Connecting state" `@Preview` — this phase wires that existing scaffolding up properly rather than inventing new state.

**Deliverables:**
- Reconnection handling in `StompRideLocationSocket` (from Phase C): retry with backoff, call `onError` appropriately so the existing `isConnected`/`error` state reflects it
- Fallback polling: if the socket stays disconnected past a threshold, fall back to polling `getTripById` on an interval
- Confirm the existing "Connecting state" preview's UI actually matches what happens live, not just in `@Preview`

**Acceptance Criteria:**
- [ ] Toggling airplane mode mid-trip doesn't crash the screen, shows the existing "connecting" UI, and recovers when connectivity returns
- [ ] Fallback polling keeps the screen reasonably live if the socket can't reconnect
- [ ] `error` field surfaces a real, useful message rather than staying null on failure

---

## Summary Table

| Phase | Type | Files | Key Deliverable |
|-------|------|-------|------------------|
| A | Modify | `TrackingScreen.kt` | Tile source + custom marker icons |
| B | Modify + extend | `TrackingScreen.kt`, `TrackingViewModel.kt` | Marker animation, dual polyline, stop markers |
| C | New (interface already exists) | `StompRideLocationSocket.kt` | Real STOMP implementation |
| D | Modify | `TrackingViewModel.kt`, `TripApi.kt` | Real trip data replacing hardcoded values |
| E | Modify | `TrackingScreen.kt`, `TrackingViewModel.kt` | Reconnection + fallback polling |
