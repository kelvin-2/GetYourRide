# Offer Ride Location Suggestions Implementation

This document describes how the `OfferRideScreen` was updated to support location suggestions using the Nominatim-backed geocoding API.

## Changes Overview

### 1. Shared Location State
The `LocationFieldState` data class was moved from `CarpoolSearchViewModel.kt` to its own file: `app/src/main/java/com/example/getyourride/viewmodel/LocationFieldState.kt`. This allows it to be shared between `CarpoolSearchViewModel`, `StopSearchViewModel`, and `OfferRideViewModel`.

### 2. ViewModel Integration
`OfferRideViewModel` in `AUseCaseViewModels.kt` was updated to:
- Accept `GeocodingRepository` in its constructor.
- Maintain `pickup` and `destination` state flows of type `LocationFieldState`.
- Implement debounced query observation (500ms) to fetch suggestions from the repository when the user types at least 3 characters.
- Provide event handlers for text changes and suggestion selection.

### 3. UI Implementation
`OfferRideScreen.kt` was updated to:
- Accept the new location states and handlers.
- Replace the static `OfferTextField` for pickup and destination with a new `AutocompleteOfferField` component.
- The `AutocompleteOfferField` displays a loading indicator and a dropdown list of suggestions when available.
- Selecting a suggestion updates the field text and shows a checkmark icon to confirm the selection.

### 4. Wiring in MainActivity
`MainActivity.kt` was updated to:
- Provide the `GeocodingRepository` to the `OfferRideViewModel`.
- Pass the state flows (using `collectAsState()`) and handlers to the `OfferRideScreen` within the NavHost.

## Usage
When a driver offers a ride, they can now type their pickup and destination locations. The app will suggest matching addresses. Selecting a suggestion ensures that the location is valid and can be resolved to coordinates if needed by the backend.
