# Shuttle Driver Backend Build Guide

## Overview

This document provides step-by-step instructions for building the Spring Boot backend
endpoints required by the shuttle driver feature in the GetYourRide Android app.

**Important Rules:**
- DO NOT remove or overwrite existing code
- CHECK if an entity/repository/class already exists before creating it
- ADD new fields or methods to existing classes rather than replacing them
- Keep existing endpoints working — this is additive work only

---

## Database Reference

The backend connects to `shuttle_db` (MySQL 8). The relevant tables are:

| Table | Purpose |
|-------|---------|
| `driver` | Shuttle driver accounts (role = 'SHUTTLE_DRIVER') |
| `vehicle` | Vehicles assigned to drivers (FK: driver_id) |
| `trip` | All trips including shuttle trips (FK: driver_id, registration_number) |
| `trip_booking` | Student bookings for trips (FK: trip_id, student_id) |
| `boarding_log` | Records when students board/drop off (FK: booking_id) |

---

## Endpoints to Build

| Method | URL | Purpose |
|--------|-----|---------|
| POST | `/api/auth/driver/login` | Shuttle driver login |
| GET | `/api/shuttle-driver/profile/{driverId}` | Full profile with vehicle + trip stats |

---

## Step 1: Entity — Driver

**Check first:** Does `Driver.java` already exist in your entity package?

- If YES → make sure it has ALL the fields below. Add any missing ones.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/entity/Driver.java`

```java
package com.example.shuttledb.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "driver")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "total_trips", nullable = false)
    private Integer totalTrips;

    // --- Getters and Setters ---
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getTotalTrips() { return totalTrips; }
    public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }
}
```

---

## Step 2: Entity — Vehicle

**Check first:** Does `Vehicle.java` already exist?

- If YES → ensure it has all the fields below.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/entity/Vehicle.java`

```java
package com.example.shuttledb.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "model")
    private String model;

    @Column(name = "vehicle_year")
    private Integer vehicleYear;

    @Column(name = "colour")
    private String colour;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    // --- Getters and Setters ---
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String rn) { this.registrationNumber = rn; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(Integer vehicleYear) { this.vehicleYear = vehicleYear; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
```

---

## Step 3: Entity — Trip

**Check first:** Does `Trip.java` already exist?

- If YES → make sure it has `driverId`, `departureStop`, `destinationStop`, `status`,
  `departureTime`, and `arrivalTime` fields mapped correctly.
- If NO → create it.

**Required fields (mapped to `trip` table columns):**

```java
package com.example.shuttledb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "trip_type", nullable = false)
    private String tripType;

    @Column(name = "slot_id")
    private Long slotId;

    @Column(name = "departure_stop", nullable = false)
    private String departureStop;

    @Column(name = "destination_stop", nullable = false)
    private String destinationStop;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "available_seats")
    private Integer availableSeats;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "departure_lat")
    private Double departureLat;

    @Column(name = "departure_lng")
    private Double departureLng;

    @Column(name = "destination_lat")
    private Double destinationLat;

    @Column(name = "destination_lng")
    private Double destinationLng;

    // --- Add getters and setters for ALL fields ---
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String rn) { this.registrationNumber = rn; }

    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getDepartureStop() { return departureStop; }
    public void setDepartureStop(String departureStop) { this.departureStop = departureStop; }

    public String getDestinationStop() { return destinationStop; }
    public void setDestinationStop(String ds) { this.destinationStop = ds; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime dt) { this.departureTime = dt; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime at) { this.arrivalTime = at; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer seats) { this.availableSeats = seats; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getDepartureLat() { return departureLat; }
    public void setDepartureLat(Double lat) { this.departureLat = lat; }

    public Double getDepartureLng() { return departureLng; }
    public void setDepartureLng(Double lng) { this.departureLng = lng; }

    public Double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(Double lat) { this.destinationLat = lat; }

    public Double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(Double lng) { this.destinationLng = lng; }
}
```

---

## Step 4: Repository — DriverRepository

**Check first:** Does `DriverRepository.java` already exist?

- If YES → add the `findByEmailAndRole` method if it's not already there.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/repository/DriverRepository.java`

```java
package com.example.shuttledb.repository;

import com.example.shuttledb.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    // May already exist — needed for student driver login
    Optional<Driver> findByEmail(String email);

    // NEW — needed for shuttle driver login (only matches SHUTTLE_DRIVER role)
    Optional<Driver> findByEmailAndRole(String email, String role);
}
```

---

## Step 5: Repository — VehicleRepository

**Check first:** Does `VehicleRepository.java` already exist?

- If YES → add the `findFirstByDriverId` method if missing.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/repository/VehicleRepository.java`

```java
package com.example.shuttledb.repository;

import com.example.shuttledb.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Returns the first vehicle assigned to this driver
    Optional<Vehicle> findFirstByDriverId(Long driverId);
}
```

---

## Step 6: Repository — TripRepository (add methods)

**Check first:** `TripRepository.java` likely already exists for carpool/trip features.

- ADD these methods to the existing interface. Do NOT replace the file.

**Methods to add:**

```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// ADD these to your existing TripRepository interface:

List<Trip> findByDriverId(Long driverId);

@Query("SELECT COUNT(t) FROM Trip t WHERE t.driverId = :driverId AND UPPER(t.status) = :status")
int countByDriverIdAndStatus(@Param("driverId") Long driverId, @Param("status") String status);

@Query("SELECT t FROM Trip t WHERE t.driverId = :driverId AND UPPER(t.status) IN ('SCHEDULED', 'IN_PROGRESS', 'CONFIRMED') ORDER BY t.departureTime DESC")
List<Trip> findActiveTrips(@Param("driverId") Long driverId);
```

---

## Step 7: DTOs — ShuttleDriverDtos

**This is a NEW file.** No existing file to conflict with.

**File:** `src/main/java/com/example/shuttledb/dto/ShuttleDriverDtos.java`

```java
package com.example.shuttledb.dto;

public class ShuttleDriverDtos {

    // ── Login Request ─────────────────────────────────────────────
    public static class ShuttleDriverLoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ── Profile Response ──────────────────────────────────────────
    public static class ShuttleDriverProfileResponse {
        private Long driverId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role;
        private String joinDate;
        private Integer totalTrips;
        private Boolean isVerified;
        private VehicleResponse vehicle;
        private TripSummaryResponse tripSummary;

        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getJoinDate() { return joinDate; }
        public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
        public Integer getTotalTrips() { return totalTrips; }
        public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
        public VehicleResponse getVehicle() { return vehicle; }
        public void setVehicle(VehicleResponse vehicle) { this.vehicle = vehicle; }
        public TripSummaryResponse getTripSummary() { return tripSummary; }
        public void setTripSummary(TripSummaryResponse ts) { this.tripSummary = ts; }
    }

    // ── Vehicle Response ──────────────────────────────────────────
    public static class VehicleResponse {
        private Long vehicleId;
        private String registrationNumber;
        private String model;
        private Integer vehicleYear;
        private String colour;
        private Integer capacity;

        public Long getVehicleId() { return vehicleId; }
        public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
        public String getRegistrationNumber() { return registrationNumber; }
        public void setRegistrationNumber(String rn) { this.registrationNumber = rn; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public Integer getVehicleYear() { return vehicleYear; }
        public void setVehicleYear(Integer vy) { this.vehicleYear = vy; }
        public String getColour() { return colour; }
        public void setColour(String colour) { this.colour = colour; }
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
    }

    // ── Trip Summary Response ─────────────────────────────────────
    public static class TripSummaryResponse {
        private String currentTripRoute;
        private String currentTripStatus;
        private int scheduledTrips;
        private int inProgressTrips;
        private int completedTrips;
        private int cancelledTrips;
        private int studentsBookedToday;
        private int studentsBoardedToday;

        public String getCurrentTripRoute() { return currentTripRoute; }
        public void setCurrentTripRoute(String r) { this.currentTripRoute = r; }
        public String getCurrentTripStatus() { return currentTripStatus; }
        public void setCurrentTripStatus(String s) { this.currentTripStatus = s; }
        public int getScheduledTrips() { return scheduledTrips; }
        public void setScheduledTrips(int v) { this.scheduledTrips = v; }
        public int getInProgressTrips() { return inProgressTrips; }
        public void setInProgressTrips(int v) { this.inProgressTrips = v; }
        public int getCompletedTrips() { return completedTrips; }
        public void setCompletedTrips(int v) { this.completedTrips = v; }
        public int getCancelledTrips() { return cancelledTrips; }
        public void setCancelledTrips(int v) { this.cancelledTrips = v; }
        public int getStudentsBookedToday() { return studentsBookedToday; }
        public void setStudentsBookedToday(int v) { this.studentsBookedToday = v; }
        public int getStudentsBoardedToday() { return studentsBoardedToday; }
        public void setStudentsBoardedToday(int v) { this.studentsBoardedToday = v; }
    }
}
```

---

## Step 8: Service — ShuttleDriverService

**This is a NEW file.** Create it fresh.

**File:** `src/main/java/com/example/shuttledb/service/ShuttleDriverService.java`

```java
package com.example.shuttledb.service;

import com.example.shuttledb.dto.ShuttleDriverDtos.*;
import com.example.shuttledb.entity.Driver;
import com.example.shuttledb.entity.Trip;
import com.example.shuttledb.entity.Vehicle;
import com.example.shuttledb.repository.DriverRepository;
import com.example.shuttledb.repository.TripRepository;
import com.example.shuttledb.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShuttleDriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    public ShuttleDriverService(DriverRepository driverRepository,
                                VehicleRepository vehicleRepository,
                                TripRepository tripRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
    }

    /**
     * Authenticate shuttle driver.
     * Only allows role = 'SHUTTLE_DRIVER'.
     */
    public Driver authenticate(String email, String password) {
        Optional<Driver> driverOpt = driverRepository.findByEmailAndRole(email, "SHUTTLE_DRIVER");

        if (driverOpt.isEmpty()) {
            throw new RuntimeException("No shuttle driver account found with this email");
        }

        Driver driver = driverOpt.get();

        // Plain text comparison — replace with BCrypt when ready
        if (!driver.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!driver.getIsVerified()) {
            throw new RuntimeException("Account not verified. Contact admin.");
        }

        return driver;
    }

    /**
     * Build the full profile for a shuttle driver.
     * Pulls from driver + vehicle + trip tables.
     */
    public ShuttleDriverProfileResponse getProfile(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Build response
        ShuttleDriverProfileResponse response = new ShuttleDriverProfileResponse();
        response.setDriverId(driver.getDriverId());
        response.setFirstName(driver.getFirstName());
        response.setLastName(driver.getLastName());
        response.setEmail(driver.getEmail());
        response.setPhone(driver.getPhone());
        response.setRole(driver.getRole());
        response.setJoinDate(driver.getJoinDate() != null
                ? driver.getJoinDate().toString() : null);
        response.setTotalTrips(driver.getTotalTrips());
        response.setIsVerified(driver.getIsVerified());

        // --- Vehicle ---
        Optional<Vehicle> vehicleOpt = vehicleRepository.findFirstByDriverId(driverId);
        if (vehicleOpt.isPresent()) {
            Vehicle v = vehicleOpt.get();
            VehicleResponse vr = new VehicleResponse();
            vr.setVehicleId(v.getVehicleId());
            vr.setRegistrationNumber(v.getRegistrationNumber());
            vr.setModel(v.getModel());
            vr.setVehicleYear(v.getVehicleYear());
            vr.setColour(v.getColour());
            vr.setCapacity(v.getCapacity());
            response.setVehicle(vr);
        }

        // --- Trip Summary ---
        TripSummaryResponse summary = new TripSummaryResponse();

        int scheduled  = tripRepository.countByDriverIdAndStatus(driverId, "SCHEDULED");
        int inProgress = tripRepository.countByDriverIdAndStatus(driverId, "IN_PROGRESS");
        int completed  = tripRepository.countByDriverIdAndStatus(driverId, "COMPLETED");
        int cancelled  = tripRepository.countByDriverIdAndStatus(driverId, "CANCELLED");

        summary.setScheduledTrips(scheduled);
        summary.setInProgressTrips(inProgress);
        summary.setCompletedTrips(completed);
        summary.setCancelledTrips(cancelled);

        // Current active trip (most recent scheduled/in-progress)
        List<Trip> activeTrips = tripRepository.findActiveTrips(driverId);
        if (!activeTrips.isEmpty()) {
            Trip current = activeTrips.get(0);
            summary.setCurrentTripRoute(
                current.getDepartureStop() + " → " + current.getDestinationStop()
            );
            summary.setCurrentTripStatus(current.getStatus());
        }

        // Today's stats — set to 0 for now, will refine in boarding feature
        summary.setStudentsBookedToday(0);
        summary.setStudentsBoardedToday(0);

        response.setTripSummary(summary);
        return response;
    }
}
```

---

## Step 9: Controller — ShuttleDriverController

**This is a NEW file.** Do NOT put these endpoints in an existing controller.

**File:** `src/main/java/com/example/shuttledb/controller/ShuttleDriverController.java`

```java
package com.example.shuttledb.controller;

import com.example.shuttledb.dto.AuthResponse;
import com.example.shuttledb.dto.ShuttleDriverDtos.*;
import com.example.shuttledb.entity.Driver;
import com.example.shuttledb.service.ShuttleDriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ShuttleDriverController {

    private final ShuttleDriverService shuttleDriverService;

    public ShuttleDriverController(ShuttleDriverService shuttleDriverService) {
        this.shuttleDriverService = shuttleDriverService;
    }

    /**
     * POST /api/auth/driver/login
     *
     * Shuttle driver login.
     * Validates email + password against the driver table (role = SHUTTLE_DRIVER).
     * Returns AuthResponse compatible with the Android app's UserSession.
     */
    @PostMapping("/auth/driver/login")
    public ResponseEntity<?> login(@RequestBody ShuttleDriverLoginRequest request) {
        try {
            Driver driver = shuttleDriverService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );

            // Build AuthResponse — MUST match what the Android app expects
            AuthResponse response = new AuthResponse();
            response.setToken("shuttle-token-" + driver.getDriverId()); // Replace with JWT
            response.setType("SHUTTLE_DRIVER");   // <-- This is what routes to boarding
            response.setId(driver.getDriverId());
            response.setFirstName(driver.getFirstName());
            response.setLastName(driver.getLastName());
            response.setEmail(driver.getEmail());
            response.setStudentNumber("");         // Not applicable
            response.setPhone(driver.getPhone());
            response.setRole(driver.getRole());
            response.setIsVerified(driver.getIsVerified());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("not verified")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
            }
            if (msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
        }
    }

    /**
     * GET /api/shuttle-driver/profile/{driverId}
     *
     * Returns the full shuttle driver profile:
     * - Driver info (name, email, phone, role, join date, total trips)
     * - Assigned vehicle (reg number, model, year, colour, capacity)
     * - Trip statistics (scheduled, in progress, completed, cancelled)
     */
    @GetMapping("/shuttle-driver/profile/{driverId}")
    public ResponseEntity<?> getProfile(@PathVariable Long driverId) {
        try {
            ShuttleDriverProfileResponse profile =
                    shuttleDriverService.getProfile(driverId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
```

---

## Step 10: Update AuthResponse.java

**Check first:** `AuthResponse.java` already exists — it's used by the student login.

- DO NOT replace it. Just make sure ALL these fields exist.
- Add any missing fields/getters/setters.

**Required fields in AuthResponse:**

```java
package com.example.shuttledb.dto;

public class AuthResponse {
    private String token;
    private String type;           // "STUDENT", "DRIVER", or "SHUTTLE_DRIVER"
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String studentNumber;  // empty string for drivers
    private String phone;
    private Boolean isFunded;      // null for drivers
    private String role;           // null for students, "SHUTTLE_DRIVER" for shuttle drivers
    private Boolean isVerified;    // null for students

    // Ensure getters and setters exist for ALL fields above
}
```

**Key point:** The Android app checks `response.type == "SHUTTLE_DRIVER"` to decide
the home route. Make sure the login endpoint sets `type = "SHUTTLE_DRIVER"` (not just "DRIVER").

---

## Step 11: Security Configuration (if using Spring Security)

If you have Spring Security configured, you need to permit these new endpoints.

**Add to your SecurityConfig or WebSecurityConfig:**

```java
// Inside your SecurityFilterChain or configure(HttpSecurity http) method:
// ADD these to your existing .requestMatchers() or .antMatchers():

.requestMatchers("/api/auth/driver/login").permitAll()
.requestMatchers("/api/shuttle-driver/profile/**").authenticated()
```

If you're NOT using Spring Security (no security config), skip this step — the
endpoints will work without any changes.

---

## Testing

### Test Login (Postman / curl)

```bash
POST http://localhost:8080/api/auth/driver/login
Content-Type: application/json

{
  "email": "thabo.nkosi@shuttle.nmu.ac.za",
  "password": "password123"
}
```

**Expected response (200 OK):**

```json
{
  "token": "shuttle-token-1",
  "type": "SHUTTLE_DRIVER",
  "id": 1,
  "firstName": "Thabo",
  "lastName": "Nkosi",
  "email": "thabo.nkosi@shuttle.nmu.ac.za",
  "studentNumber": "",
  "phone": "0821234501",
  "isFunded": null,
  "role": "SHUTTLE_DRIVER",
  "isVerified": true
}
```

### Test Profile (Postman / curl)

```bash
GET http://localhost:8080/api/shuttle-driver/profile/1
Authorization: Bearer shuttle-token-1
```

**Expected response (200 OK):**

```json
{
  "driverId": 1,
  "firstName": "Thabo",
  "lastName": "Nkosi",
  "email": "thabo.nkosi@shuttle.nmu.ac.za",
  "phone": "0821234501",
  "role": "SHUTTLE_DRIVER",
  "joinDate": "2024-02-01",
  "totalTrips": 142,
  "isVerified": true,
  "vehicle": {
    "vehicleId": 1,
    "registrationNumber": "NMU001EC",
    "model": "Toyota Quantum",
    "vehicleYear": 2021,
    "colour": "White",
    "capacity": 15
  },
  "tripSummary": {
    "currentTripRoute": "North Campus → South Campus",
    "currentTripStatus": "SCHEDULED",
    "scheduledTrips": 1,
    "inProgressTrips": 0,
    "completedTrips": 0,
    "cancelledTrips": 0,
    "studentsBookedToday": 0,
    "studentsBoardedToday": 0
  }
}
```

### Test Invalid Login

```bash
POST http://localhost:8080/api/auth/driver/login
Content-Type: application/json

{
  "email": "thabo.nkosi@shuttle.nmu.ac.za",
  "password": "wrongpassword"
}
```

**Expected: 401 Unauthorized** with body `"Invalid email or password"`

---

## Available Test Accounts (from database seed data)

| Driver ID | Name | Email | Password | Role | Verified |
|-----------|------|-------|----------|------|----------|
| 1 | Thabo Nkosi | thabo.nkosi@shuttle.nmu.ac.za | password123 | SHUTTLE_DRIVER | Yes |
| 2 | Nomvula Dube | nomvula.dube@shuttle.nmu.ac.za | password123 | SHUTTLE_DRIVER | Yes |

Drivers with `is_verified = 0` (like ID 5, Luyanda Zulu) will get a 403 Forbidden response.

Student drivers (role = STUDENT_DRIVER) will NOT be able to log in through this endpoint —
they use the regular `/api/auth/student/login` endpoint.

---

## File Checklist

Use this to track what you've created/updated:

| # | File | Action | Done |
|---|------|--------|------|
| 1 | `entity/Driver.java` | Check exists → add missing fields | [ ] |
| 2 | `entity/Vehicle.java` | Check exists → add missing fields | [ ] |
| 3 | `entity/Trip.java` | Check exists → add missing fields | [ ] |
| 4 | `repository/DriverRepository.java` | Add `findByEmailAndRole` | [ ] |
| 5 | `repository/VehicleRepository.java` | Add `findFirstByDriverId` | [ ] |
| 6 | `repository/TripRepository.java` | Add 3 query methods | [ ] |
| 7 | `dto/ShuttleDriverDtos.java` | Create new file | [ ] |
| 8 | `service/ShuttleDriverService.java` | Create new file | [ ] |
| 9 | `controller/ShuttleDriverController.java` | Create new file | [ ] |
| 10 | `dto/AuthResponse.java` | Add missing fields (type, role, isVerified) | [ ] |
| 11 | Security config (if applicable) | Permit new endpoints | [ ] |

---

## How Android App Connects

```
┌─────────────────────────────────────────────────────────────────┐
│                       ANDROID APP                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  LoginScreen                                                    │
│       │                                                         │
│       ▼                                                         │
│  POST /api/auth/driver/login  ──────►  AuthResponse             │
│       │                                (type = "SHUTTLE_DRIVER")│
│       ▼                                                         │
│  homeRouteFor() returns "shuttle_driver_boarding"               │
│       │                                                         │
│       ▼                                                         │
│  ShuttleDriverBoardingScreen  (HOME PAGE)                       │
│       │                                                         │
│       ├──► Profile tab clicked                                  │
│       ▼                                                         │
│  ShuttleDriverProfileScreen                                     │
│       │                                                         │
│       ▼                                                         │
│  GET /api/shuttle-driver/profile/{driverId}                     │
│       │                                                         │
│       ▼                                                         │
│  Displays: Driver info + Vehicle + Trip statistics              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Notes

- The password comparison is plain text for now (`driver.getPassword().equals(password)`).
  When you're ready to add BCrypt, encode passwords in the DB and use
  `BCryptPasswordEncoder.matches()` instead.
- The `token` field is a placeholder string. If you already have JWT generation in
  your project, use that instead. The Android app just stores it and sends it back
  in the `Authorization: Bearer <token>` header.
- The `studentsBookedToday` and `studentsBoardedToday` fields are set to 0 for now.
  These will be implemented when we build the boarding feature backend (joining
  `trip_booking` and `boarding_log` tables filtered by today's date).
- Make sure your `application.properties` has the correct database connection:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shuttle_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```
