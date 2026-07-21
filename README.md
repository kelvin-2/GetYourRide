# GetYourRide - Shuttle Booking Feature

This repository contains the GetYourRide Android application, which now includes a fully integrated Shuttle Booking flow for NSFAS-funded students.

## New Feature: Shuttle Booking

The Shuttle Booking feature allows students to reserve seats on predetermined campus shuttle routes. Unlike the carpool feature which uses dynamic geocoding, the shuttle feature uses a fixed list of stops and time slots provided by the backend.

### Key Components

- **UI**:
    - `ShuttleHomeScreen`: The main entry point for shuttle-related activities.
    - `BookShuttleScreen`: Where users pick their pickup/destination and departure time.
    - `ShuttleStopSelectionScreen`: A dedicated search screen for selecting from fixed campus stops.
- **Data Layer**:
    - `ShuttleApi`: Retrofit interface defining endpoints for stops and time slots.
    - `ShuttleRepository`: Manages data fetching from the backend and provides fallback data for offline/mock usage.
    - `ShuttleDtos`: Data Transfer Objects for JSON parsing.
- **Business Logic**:
    - `ScheduleRideViewModel`: Manages the state of the booking process, including time slot selection and booking confirmation.
    - `ShuttleStopSearchViewModel`: Handles real-time filtering of the fixed stops list.

## Recent Changes

For a detailed list of all technical changes, refactorings, and bug fixes, please see [CHANGES.md](CHANGES.md).

### Summary of Major Updates:
1.  **Backend Integration**: Wired the shuttle flow to `http://<YOUR_IP>:8080/api/shuttle-stops`.
2.  **Case Sensitivity Fixes**: Refactored the `shuttle` package to lowercase to prevent build issues.
3.  **UI/UX Improvements**: Added editable location cards, loading indicators, and snackbar error messaging.
4.  **Architecture Documentation**: Updated [docs/architecture.md](docs/architecture.md) to reflect the new feature structure.

## Technical Details

- **Retrofit**: Used for all backend communication.
- **Compose**: Entire UI built with Jetpack Compose.
- **Coroutines/Flow**: Used for asynchronous data fetching and state management.

For more information on the overall project structure, refer to the [Architecture Documentation](docs/architecture.md).
