# GetYourRide Architecture Documentation

This document provides an overview of the architecture and directory structure of the **GetYourRide** Android application.

## Overview

The application follows modern Android development practices, utilizing **Jetpack Compose** for the UI and following a variant of the **MVVM (Model-View-ViewModel)** architectural pattern. It is structured to separate concerns between data handling, business logic, and UI presentation.

---

## Directory Structure

The project's source code is organized under `com.example.getyourride`:

### 1. `data/`
Responsible for data procurement and persistence.
- **`remote/`**: Contains API interfaces (Retrofit) for backend communication (e.g., `StudentAuthApi`).
- **`repository/`**: Implementation of the Repository pattern, abstracting data sources from the rest of the app (e.g., `TripRepository`, `StudentAuthRepository`).
- **`mapper/`**: Utility classes to convert Data Transfer Objects (DTOs) to Domain or UI models.
- **`model/`**: Data classes representing the raw data structures.

### 2. `viewmodel/`
Acts as the bridge between the Data layer and the UI layer.
- Contains `ViewModel` classes (e.g., `AuthViewModel`, `RideViewModel`, `TripBookingViewModel`) that hold UI state and handle user interactions by communicating with repositories.
- Includes `ViewModelFactory` classes for dependency injection into ViewModels.

### 3. `ui/`
Handles everything related to the user interface.
- **`screens/`**: Individual screen composables, organized by feature:
    - `Carpool/`: Home screen, My Rides, etc.
    - `Rides/`: Ride request and details.
    - `Tracking/`: Live ride tracking.
    - `shuttle/`: Shuttle booking flow for NSFAS students, including home and stop selection.
    - `shuttleDriver/`: Specific views for shuttle drivers.
- **`components/`**: Reusable UI widgets used across multiple screens (e.g., `GyrRoutes`, `ConfirmationDialog`).
    - `shuttle/components/`: Specific reusable components for the shuttle flow like `DepartureTimeGrid`.
- **`theme/`**: Theming configuration including colors, typography, and shapes.

### 4. `network/`
Contains network-specific configurations and services, such as `SpringBootApiService` for general API calls.

### 5. `di/`
Handles Dependency Injection (manual or framework-based).
- **`NetworkModule`**: Centralized location for providing singleton instances of Retrofit APIs and other network components.

### 6. Root Files
- **`MainActivity.kt`**: The entry point of the app, hosting the `NavHost` for screen navigation.
- **`UserSession.kt`**: Manages the current user's session state and authentication tokens.
- **`GetYourRideApp.kt`**: The custom `Application` class.

---

## Key Architectural Patterns

- **Unidirectional Data Flow (UDF)**: ViewModels expose state (often via `MutableState` or `StateFlow`), and UI components emit events back to the ViewModel.
- **Repository Pattern**: Centralizes data access logic to provide a clean API for the ViewModels. This includes repositories like `ShuttleRepository` which now communicates with the backend via `ShuttleApi`.
- **Fixed Stop Selection**: Unlike the Carpool flow which uses a Geocoding service for dynamic address searching, the Shuttle flow uses a predetermined list of campus stops fetched from the backend `/api/shuttle-stops` endpoint.
- **Navigation Compose**: Uses a single-activity architecture where navigation between "screens" (composables) is managed by a `NavController`.

---

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Asynchrony**: Kotlin Coroutines & Flow
- **Networking**: Retrofit & OkHttp
- **Image Loading**: Coil
- **Maps**: Osmdroid
- **Location**: Google Play Services Location
- **Dependency Management**: Gradle with Version Catalogs (BOM)
