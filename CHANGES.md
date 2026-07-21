# Project Changes - Shuttle Booking Flow Updates

This document tracks the recent changes made to the GetYourRide application, specifically focusing on the Shuttle Booking flow.

## 1. Directory Refactoring & Typos
- **Typo Fix**: Renamed the misspelled directory `componets` to `components` across the shuttle screen package.
- **Package Case Consistency**: Standardized the shuttle screen package name to lowercase `shuttle` (from `Shuttle`) to ensure compatibility with Gradle and avoid case-sensitivity issues on different operating systems.
- **File Renaming**: Renamed `ShuttleHomescreen.kt` to `ShuttleHomeScreen.kt` to match the composable function naming convention.

## 2. Shuttle Booking Enhancements
- **Predetermined Stops**: Updated `ShuttleRepository` to include a hardcoded list of NMU campus stops. This bypasses the need for an external Geocoding service for the shuttle flow, as the stops are fixed.
- **Editable Location Fields**: Modified `TripLocationCard` to make the Pickup and Destination labels clickable.
- **New Search Logic**:
    - Created `ShuttleStopSearchViewModel` to handle filtering the predetermined stops list based on user input.
    - Created `ShuttleStopSelectionScreen`, a dedicated UI for picking a stop from the filtered list.
- **Navigation Integration**: Updated `MainActivity` to route shuttle stop selection to the new `ShuttleStopSelectionScreen`.

## 3. Error Handling & UX
- **ViewModel State**: Added `errorMessage` and `isLoading` states to `ScheduleRideViewModel` and `ShuttleStopSearchViewModel`.
- **Snackbar Feedback**: Integrated `SnackbarHost` in `BookShuttleScreen` to provide real-time feedback to the user when errors occur during the booking process.
- **Loading Indicators**: Added `CircularProgressIndicator` to screens to improve UX during simulated network delays.

## 4. Documentation
- **Architecture Update**: Updated `docs/architecture.md` to reflect the new `shuttle` screen structure and the distinction between the Carpool (Dynamic Geocoding) and Shuttle (Fixed Stops) selection patterns.
