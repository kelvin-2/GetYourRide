# GetYourRide - Project Architecture & File Structure

This document outlines the project structure of the **GetYourRide** Android application. It serves as a guide for where to place new files and how the existing codebase is organized.

## 📁 Directory Structure

The project follows a modular structure based on **Clean Architecture** principles and **MVVM** pattern.

### 📍 Root: `app/src/main/java/com/example/getyourride`

| Directory | Purpose |
| :--- | :--- |
| `ui/` | All UI-related code (Screens, Components, Theme). |
| `viewmodel/` | ViewModel classes that handle UI logic and state. |
| `data/` | Data layer: Repositories, Models (DTOs), and Mappers. |
| `network/` | Retrofit API interfaces and Network service definitions. |
| `di/` | Dependency Injection modules (NetworkModule, etc.). |
| `domain/` | (Optional) Business logic and Use Cases. |

---

## 🎨 UI Layer (`ui/`)

Located at: `com.example.getyourride.ui`

- **`screens/`**: Full-screen Composable components.
    - `Carpool/`: Screens related to student-led carpooling.
    - `Shuttle/`: Screens specifically for NSFAS-funded shuttle services.
    - `Tracking/`: Real-time ride tracking screens.
    - `shuttleDriver/`: Screens for the shuttle driver interface.
- **`components/`**: Reusable UI widgets (Buttons, Cards, TopBars, BottomNav).
- **`theme/`**: Theme definitions (Color, Type, Shape, and the `GetYourRideTheme`).

---

## ⚙️ Logic Layer (`viewmodel/`)

Located at: `com.example.getyourride.viewmodel`

All ViewModels should go here. They are responsible for:
- Holding UI state.
- Communicating with Repositories.
- Handling user actions.

*Example: `ShuttleViewModel.kt`, `AuthViewModel.kt`.*

---

## 💾 Data Layer (`data/`)

Located at: `com.example.getyourride.data`

- **`repository/`**: Classes that abstract data sources (API or Local DB).
- **`model/`**: Data classes representing API responses or internal data.
- **`mapper/`**: Extension functions to convert Data Models to UI Models.
- **`remote/`**: API interface definitions (Retrofit).

---

## 🚀 Where Should I Put My Code?

| If you are adding... | Put it in... |
| :--- | :--- |
| A new screen | `ui/screens/` |
| A reusable button or card | `ui/components/` |
| New logic or state handling | `viewmodel/` |
| A new API endpoint | `data/remote/` or `network/` |
| A new data source/fetcher | `data/repository/` |
| Colors or Fonts | `ui/theme/` |

---

## 🗺️ Main Entry Point

- **`MainActivity.kt`**: Contains the `NavHost` which defines the navigation graph and routes for the entire app.
- **`UserSession.kt`**: Manages global user state (Login status, NSFAS funding status).

---

## 🛠️ Build & Resources
- **`res/drawable`**: Icons, images, and XML shapes.
- **`res/values`**: Strings, colors, and themes (XML based).
- **`build.gradle.kts`**: Dependency management.
