# Project Changes - Shuttle Booking & Navigation Updates

This document tracks the recent changes made to the GetYourRide application, specifically focusing on the Shuttle Booking flow, backend integration, and navigation architecture.

## 1. Directory Refactoring & Typos
- **Typo Fix**: Renamed the misspelled directory `componets` to `components` across the shuttle screen package.
- **Package Case Consistency**: Standardized the shuttle screen package name to lowercase `shuttle` (from `Shuttle`) to ensure compatibility with Gradle and avoid case-sensitivity issues on different operating systems.
- **File Renaming**: Renamed `ShuttleHomescreen.kt` to `ShuttleHomeScreen.kt` to match the composable function naming convention.

## 2. Shuttle Booking Enhancements
- **Predetermined Stops**: Updated `ShuttleRepository` to fetch campus stops from the backend. This bypasses the need for an external Geocoding service for the shuttle flow, as the stops are fixed.
- **Editable Location Fields**: Modified `TripLocationCard` to make the Pickup and Destination labels clickable.
- **New Search Logic**:
    - Created `ShuttleStopSearchViewModel` to handle filtering the predetermined stops list based on user input.
    - Created `ShuttleStopSelectionScreen`, a dedicated UI for picking a stop from the filtered list.
- **Sectioned Time Slots**: Redesigned `DepartureTimeGrid` to group time slots into "Morning" and "Afternoon" sections with headers. Improved labels by removing seconds (e.g., "06:45:00" -> "06:45").

## 3. Error Handling & UX
- **ViewModel State**: Added `errorMessage` and `isLoading` states to `ScheduleRideViewModel` and `ShuttleStopSearchViewModel`.
- **Snackbar Feedback**: Integrated `SnackbarHost` in `BookShuttleScreen` to provide real-time feedback to the user when errors occur during the booking process.
- **Loading Indicators**: Added `CircularProgressIndicator` to screens to improve UX during network calls.
- **Scrolling Support**: Added vertical scroll support to the shuttle booking screen to accommodate more complex layouts.

## 4. Backend Integration
- **API Definition**: Created `ShuttleApi` Retrofit interface with endpoints for `getAllStops()` and `getAllTimeSlots()`.
- **Data Models**: Added `ShuttleStopResponse`, `ShuttleTimeSlotResponse`, and a structured `ShuttleTimeSlot` UI model in `ShuttleDtos.kt`.
- **Network Module**: Wired `ShuttleApi` into `NetworkModule` as a lazy singleton with automatic Bearer token injection.
- **Repository Update**: Refactored `ShuttleRepository` to use `ShuttleApi` for real network calls, including robust fallback data for offline scenarios.

## 5. Navigation Architecture Separation
- **Dynamic Bottom Nav**: Refactored `GyrBottomNav` to dynamically switch between Carpool and Shuttle tabs based on the student's NSFAS status (`UserSession.isFunded`).
- **Isolated Routes**: Added `GyrRoutes.SHUTTLE_HOME` and `GyrRoutes.SHUTTLE_RIDES` to ensure the two flows don't share paths or accidentally redirect back to the wrong home screen.
- **Unified Layout**: Updated `StudentLayout` to manage the dynamic navigation state, providing a consistent experience across all student screens.

## 6. Documentation
- **Architecture Update**: Updated `docs/architecture.md` to reflect the new `shuttle` package structure, backend wiring, and the "Fixed Stop Selection" pattern.
- **Project README**: Created a root `README.md` for a high-level overview of the app's features and recent technical milestones.
