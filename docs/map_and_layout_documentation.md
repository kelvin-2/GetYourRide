# Documentation: Map APIs and Layout Architecture

This document explains the Map APIs used in the project and how the layout system is structured.

## 1. Map APIs

The project uses **osmdroid** for map functionality.

### osmdroid
- **Library**: `org.osmdroid:osmdroid-android:6.1.20`
- **Purpose**: Provides a free, open-source alternative to Google Maps.
- **Key Features**:
    - Uses OpenStreetMap (OSM) tiles.
    - No API key required for basic usage.
    - Highly customizable with overlays (Markers, Polylines).
- **Implementation Details**:
    - The `OsmMapView` component in `TrackingScreen.kt` initializes the `MapView`.
    - It uses `TileSourceFactory.MAPNIK` for standard map tiles.
    - Markers are used for the driver's location and the destination.
    - A `Polyline` is used to visualize the route between the driver and the destination.

---

## 2. Layout Architecture

The app uses a modular layout system to maintain a consistent UI while allowing for screen-specific modifications.

### `GyrScaffold` (Generic)
Located in `GyrBottomNav.kt`, this is a basic wrapper around the Material 3 `Scaffold`. It automatically attaches the `GyrBottomNav`.

### `StudentLayout` (Standard)
The primary layout for most student-facing screens.
- Includes a top bar (`GyrTopBar`).
- Includes the full bottom navigation (`GyrBottomNav`).
- Supports optional features like a floating action button or a notification bell.

### `ShuttleLayout` (Specialized)
Created to fulfill the requirement for a cleaner, shuttle-focused experience.
- **No Top Bar**: Provides more screen real estate for shuttle content.
- **Restricted Bottom Nav**: Automatically configures `GyrBottomNav` to hide the "Tracking" tab, as shuttles follow fixed routes and don't require individual tracking.

### `GyrBottomNav` (Configurable)
The bottom navigation bar is the central hub for app navigation. It now supports multiple modes:
- **Default**: Home, Rides, Track, Profile.
- **Funded (NSFAS)**: Includes shuttle-specific home and rides tabs.
- **Shuttle-Only**: Home, Rides, Profile (Tracking hidden).

---

## 3. How It All Works Together

1. **Navigation**: `MainActivity.kt` defines the `NavHost` and routes.
2. **Screen Composition**: Each screen (e.g., `ShuttleHomeScreen`) is wrapped in a layout component (`ShuttleLayout`).
3. **Dynamic UI**: Layouts pass the `currentRoute` to `GyrBottomNav` to highlight the active tab.
4. **State Management**: ViewModels (e.g., `TrackingViewModel`) handle the business logic and provide data (like `GeoPoint` coordinates) to the UI components.
