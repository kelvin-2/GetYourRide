# Shuttle Driver Boarding — Backend Build Guide

## Overview

This document provides step-by-step instructions for building the Spring Boot backend
endpoints required by the shuttle driver **boarding screen** in the GetYourRide Android app.

The boarding screen is the shuttle driver's **home page**. It shows:
- The driver's current/next active trip
- All students who booked that trip
- A button to mark each student as boarded (updates `boarding_log`)

**Important Rules:**
- DO NOT remove or overwrite existing code
- CHECK if an entity/repository/class already exists before creating it
- ADD new methods to existing classes rather than replacing them
- The profile backend guide should have been completed first — entities and repositories from there are reused here

---

## Database Tables Used

| Table | Purpose |
|-------|---------|
| `trip` | Find the driver's active trip (status = SCHEDULED or IN_PROGRESS) |
| `trip_booking` | Students who booked a specific trip |
| `student` | Student details (name, student number) |
| `boarding_log` | Tracks when a student boards (boarded_at) and drops off |
| `vehicle` | Vehicle capacity for the trip |

---

## Endpoints to Build

| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/api/shuttle-driver/{driverId}/active-trip` | Get the current/next active trip |
| GET | `/api/shuttle-driver/trip/{tripId}/students` | Get all booked students for a trip |
| POST | `/api/shuttle-driver/boarding/mark` | Mark a student as boarded |

---

## Step 1: Entity — TripBooking

**Check first:** Does `TripBooking.java` already exist?

- If YES → make sure it has the fields below.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/entity/TripBooking.java`

```java
package com.example.shuttledb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_booking")
public class TripBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "booking_status")
    private String bookingStatus;  // "Pending", "Confirmed", "Cancelled"

    // --- Getters and Setters ---
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
}
```

---

## Step 2: Entity — Student

**Check first:** Does `Student.java` already exist?

- If YES → make sure it has `studentId`, `firstName`, `lastName`, `studentNumber`.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/entity/Student.java`

```java
package com.example.shuttledb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "student_number", nullable = false, unique = true)
    private String studentNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_funded", nullable = false)
    private Boolean isFunded;

    @Column(name = "password", nullable = false)
    private String password;

    // --- Getters and Setters ---
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsFunded() { return isFunded; }
    public void setIsFunded(Boolean isFunded) { this.isFunded = isFunded; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

---

## Step 3: Entity — BoardingLog

**Check first:** Does `BoardingLog.java` already exist?

- If YES → ensure it has `logId`, `bookingId`, `boardedAt`, `droppedOffAt`.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/entity/BoardingLog.java`

```java
package com.example.shuttledb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "boarding_log")
public class BoardingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "boarded_at")
    private LocalDateTime boardedAt;

    @Column(name = "dropped_off_at")
    private LocalDateTime droppedOffAt;

    // --- Getters and Setters ---
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getBoardedAt() { return boardedAt; }
    public void setBoardedAt(LocalDateTime boardedAt) { this.boardedAt = boardedAt; }

    public LocalDateTime getDroppedOffAt() { return droppedOffAt; }
    public void setDroppedOffAt(LocalDateTime droppedOffAt) { this.droppedOffAt = droppedOffAt; }
}
```

---

## Step 4: Repository — TripBookingRepository

**Check first:** Does `TripBookingRepository.java` already exist?

- If YES → add the methods below if they're not already there.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/repository/TripBookingRepository.java`

```java
package com.example.shuttledb.repository;

import com.example.shuttledb.entity.TripBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripBookingRepository extends JpaRepository<TripBooking, Long> {

    // Find all bookings for a specific trip
    List<TripBooking> findByTripId(Long tripId);

    // Count confirmed bookings for a trip
    int countByTripIdAndBookingStatus(Long tripId, String bookingStatus);
}
```

---

## Step 5: Repository — StudentRepository

**Check first:** Does `StudentRepository.java` already exist?

- If YES → make sure `findById` works (it does by default with JpaRepository).
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/repository/StudentRepository.java`

```java
package com.example.shuttledb.repository;

import com.example.shuttledb.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentNumber(String studentNumber);
}
```

---

## Step 6: Repository — BoardingLogRepository

**Check first:** Does `BoardingLogRepository.java` already exist?

- If YES → add the methods below.
- If NO → create it.

**File:** `src/main/java/com/example/shuttledb/repository/BoardingLogRepository.java`

```java
package com.example.shuttledb.repository;

import com.example.shuttledb.entity.BoardingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BoardingLogRepository extends JpaRepository<BoardingLog, Long> {

    // Find a boarding log entry by booking ID
    Optional<BoardingLog> findByBookingId(Long bookingId);
}
```

---

## Step 7: Repository — TripRepository (add methods)

**Check first:** `TripRepository.java` should already exist from the profile backend guide.

- ADD these methods if not already there. Do NOT replace the file.

**Methods to add:**

```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

// ADD to your existing TripRepository:

/**
 * Find the most recent active trip for a shuttle driver.
 * Active = status is SCHEDULED, IN_PROGRESS, or CONFIRMED.
 * Orders by departure_time DESC so the most upcoming one is first.
 */
@Query("SELECT t FROM Trip t WHERE t.driverId = :driverId " +
       "AND UPPER(t.status) IN ('SCHEDULED', 'IN_PROGRESS', 'CONFIRMED') " +
       "ORDER BY t.departureTime ASC")
List<Trip> findActiveTrips(@Param("driverId") Long driverId);

/**
 * Find a specific trip by ID.
 * Already provided by JpaRepository.findById() — no extra method needed.
 */
```

**Note:** If you already added `findActiveTrips` from the profile guide, skip this step.

---

## Step 8: DTOs — Add Boarding DTOs

**Check first:** `ShuttleDriverDtos.java` should already exist from the profile guide.

- ADD these inner classes to the existing file. Do NOT remove existing classes.

**Add these classes inside `ShuttleDriverDtos.java`:**

```java
    // ── Active Trip Response ──────────────────────────────────────────
    public static class ActiveTripResponse {
        private Long tripId;
        private String departureStop;
        private String destinationStop;
        private String departureTime;
        private String arrivalTime;
        private String status;
        private int capacity;
        private String registrationNumber;
        private int totalBooked;
        private int totalBoarded;

        public Long getTripId() { return tripId; }
        public void setTripId(Long tripId) { this.tripId = tripId; }
        public String getDepartureStop() { return departureStop; }
        public void setDepartureStop(String ds) { this.departureStop = ds; }
        public String getDestinationStop() { return destinationStop; }
        public void setDestinationStop(String ds) { this.destinationStop = ds; }
        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String dt) { this.departureTime = dt; }
        public String getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(String at) { this.arrivalTime = at; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public String getRegistrationNumber() { return registrationNumber; }
        public void setRegistrationNumber(String rn) { this.registrationNumber = rn; }
        public int getTotalBooked() { return totalBooked; }
        public void setTotalBooked(int totalBooked) { this.totalBooked = totalBooked; }
        public int getTotalBoarded() { return totalBoarded; }
        public void setTotalBoarded(int totalBoarded) { this.totalBoarded = totalBoarded; }
    }

    // ── Booked Student Response ───────────────────────────────────────
    public static class BoardedStudentResponse {
        private Long bookingId;
        private Long studentId;
        private String firstName;
        private String lastName;
        private String studentNumber;
        private String bookingStatus;
        private String boardedAt;  // null if not boarded yet

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String sn) { this.studentNumber = sn; }
        public String getBookingStatus() { return bookingStatus; }
        public void setBookingStatus(String bs) { this.bookingStatus = bs; }
        public String getBoardedAt() { return boardedAt; }
        public void setBoardedAt(String boardedAt) { this.boardedAt = boardedAt; }
    }

    // ── Mark as Boarded Request ───────────────────────────────────────
    public static class MarkAsBoardedRequest {
        private Long bookingId;

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    }

    // ── Mark as Boarded Response ──────────────────────────────────────
    public static class MarkAsBoardedResponse {
        private boolean success;
        private String message;
        private String boardedAt;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getBoardedAt() { return boardedAt; }
        public void setBoardedAt(String boardedAt) { this.boardedAt = boardedAt; }
    }
```

---

## Step 9: Service — Add Boarding Methods to ShuttleDriverService

**Check first:** `ShuttleDriverService.java` should already exist from the profile guide.

- ADD these methods. Do NOT remove existing `authenticate()` or `getProfile()`.

**Inject these additional repositories in the constructor (if not already there):**

```java
private final TripBookingRepository tripBookingRepository;
private final StudentRepository studentRepository;
private final BoardingLogRepository boardingLogRepository;
```

**Updated constructor:**

```java
public ShuttleDriverService(DriverRepository driverRepository,
                            VehicleRepository vehicleRepository,
                            TripRepository tripRepository,
                            TripBookingRepository tripBookingRepository,
                            StudentRepository studentRepository,
                            BoardingLogRepository boardingLogRepository) {
    this.driverRepository = driverRepository;
    this.vehicleRepository = vehicleRepository;
    this.tripRepository = tripRepository;
    this.tripBookingRepository = tripBookingRepository;
    this.studentRepository = studentRepository;
    this.boardingLogRepository = boardingLogRepository;
}
```

**ADD these methods:**

```java
import com.example.shuttledb.dto.ShuttleDriverDtos.*;
import com.example.shuttledb.entity.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

    /**
     * Get the current/next active trip for this shuttle driver.
     * Returns the nearest scheduled or in-progress trip.
     */
    public ActiveTripResponse getActiveTrip(Long driverId) {
        List<Trip> activeTrips = tripRepository.findActiveTrips(driverId);

        if (activeTrips.isEmpty()) {
            throw new RuntimeException("No active trip assigned. Check your schedule.");
        }

        Trip trip = activeTrips.get(0);  // Nearest upcoming/active trip

        // Get vehicle capacity
        Optional<Vehicle> vehicleOpt = vehicleRepository
                .findFirstByDriverId(driverId);
        int capacity = vehicleOpt.map(Vehicle::getCapacity).orElse(0);

        // Count booked students
        List<TripBooking> bookings = tripBookingRepository.findByTripId(trip.getTripId());
        int totalBooked = bookings.size();

        // Count boarded students (those with a boarding_log entry where boarded_at != null)
        int totalBoarded = 0;
        for (TripBooking booking : bookings) {
            Optional<BoardingLog> log = boardingLogRepository.findByBookingId(booking.getBookingId());
            if (log.isPresent() && log.get().getBoardedAt() != null) {
                totalBoarded++;
            }
        }

        // Build response
        ActiveTripResponse response = new ActiveTripResponse();
        response.setTripId(trip.getTripId());
        response.setDepartureStop(trip.getDepartureStop());
        response.setDestinationStop(trip.getDestinationStop());
        response.setDepartureTime(formatDateTime(trip.getDepartureTime()));
        response.setArrivalTime(formatDateTime(trip.getArrivalTime()));
        response.setStatus(trip.getStatus());
        response.setCapacity(capacity);
        response.setRegistrationNumber(trip.getRegistrationNumber());
        response.setTotalBooked(totalBooked);
        response.setTotalBoarded(totalBoarded);

        return response;
    }

    /**
     * Get all booked students for a trip, with their boarding status.
     */
    public List<BoardedStudentResponse> getBookedStudents(Long tripId) {
        List<TripBooking> bookings = tripBookingRepository.findByTripId(tripId);
        List<BoardedStudentResponse> result = new ArrayList<>();

        for (TripBooking booking : bookings) {
            // Get student info
            Optional<Student> studentOpt = studentRepository.findById(booking.getStudentId());
            if (studentOpt.isEmpty()) continue;

            Student student = studentOpt.get();

            // Check boarding status
            Optional<BoardingLog> logOpt = boardingLogRepository.findByBookingId(booking.getBookingId());
            String boardedAt = null;
            if (logOpt.isPresent() && logOpt.get().getBoardedAt() != null) {
                boardedAt = formatDateTime(logOpt.get().getBoardedAt());
            }

            // Build response
            BoardedStudentResponse dto = new BoardedStudentResponse();
            dto.setBookingId(booking.getBookingId());
            dto.setStudentId(student.getStudentId());
            dto.setFirstName(student.getFirstName());
            dto.setLastName(student.getLastName());
            dto.setStudentNumber(student.getStudentNumber());
            dto.setBookingStatus(booking.getBookingStatus());
            dto.setBoardedAt(boardedAt);

            result.add(dto);
        }

        return result;
    }

    /**
     * Mark a student as boarded.
     * Creates a boarding_log entry if one doesn't exist,
     * or updates the existing one with boarded_at = now.
     */
    public MarkAsBoardedResponse markAsBoarded(Long bookingId) {
        // Verify booking exists
        Optional<TripBooking> bookingOpt = tripBookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found with ID: " + bookingId);
        }

        // Find or create boarding log
        Optional<BoardingLog> existingLog = boardingLogRepository.findByBookingId(bookingId);
        BoardingLog log;

        if (existingLog.isPresent()) {
            log = existingLog.get();
        } else {
            log = new BoardingLog();
            log.setBookingId(bookingId);
        }

        // Set boarded time
        LocalDateTime now = LocalDateTime.now();
        log.setBoardedAt(now);
        boardingLogRepository.save(log);

        // Build response
        MarkAsBoardedResponse response = new MarkAsBoardedResponse();
        response.setSuccess(true);
        response.setMessage("Student marked as boarded successfully");
        response.setBoardedAt(formatDateTime(now));

        return response;
    }

    /**
     * Helper to format LocalDateTime as a readable string.
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
```

---

## Step 10: Controller — Add Boarding Endpoints

**Check first:** `ShuttleDriverController.java` should already exist from the profile guide.

- ADD these endpoint methods. Do NOT remove existing `login()` or `getProfile()`.

**Add these methods to your existing `ShuttleDriverController`:**

```java
import com.example.shuttledb.dto.ShuttleDriverDtos.*;
import java.util.List;

    /**
     * GET /api/shuttle-driver/{driverId}/active-trip
     *
     * Returns the current/next active trip for this shuttle driver.
     * The Android boarding screen calls this when it opens.
     */
    @GetMapping("/shuttle-driver/{driverId}/active-trip")
    public ResponseEntity<?> getActiveTrip(@PathVariable Long driverId) {
        try {
            ActiveTripResponse trip = shuttleDriverService.getActiveTrip(driverId);
            return ResponseEntity.ok(trip);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/shuttle-driver/trip/{tripId}/students
     *
     * Returns all booked students for a specific trip with boarding status.
     * Called after active-trip returns a tripId.
     */
    @GetMapping("/shuttle-driver/trip/{tripId}/students")
    public ResponseEntity<?> getBookedStudents(@PathVariable Long tripId) {
        try {
            List<BoardedStudentResponse> students =
                    shuttleDriverService.getBookedStudents(tripId);
            return ResponseEntity.ok(students);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * POST /api/shuttle-driver/boarding/mark
     *
     * Marks a student as boarded.
     * Creates/updates boarding_log.boarded_at for this booking.
     *
     * Request body: { "bookingId": 123 }
     */
    @PostMapping("/shuttle-driver/boarding/mark")
    public ResponseEntity<?> markAsBoarded(@RequestBody MarkAsBoardedRequest request) {
        try {
            MarkAsBoardedResponse result =
                    shuttleDriverService.markAsBoarded(request.getBookingId());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
```

---

## Step 11: Security Configuration (if using Spring Security)

If you have Spring Security, permit the new endpoints:

```java
// ADD to your existing .requestMatchers():
.requestMatchers("/api/shuttle-driver/*/active-trip").authenticated()
.requestMatchers("/api/shuttle-driver/trip/*/students").authenticated()
.requestMatchers("/api/shuttle-driver/boarding/mark").authenticated()
```

If NOT using Spring Security, skip this step.

---

## Step 12: Insert Test Booking Data

To test the boarding screen, you need at least one trip booking.
Run this SQL to create test bookings for trip ID 24 (Thabo Nkosi's shuttle trip):

```sql
-- Insert test bookings for trip 24 (NMU shuttle North Campus → South Campus)
INSERT INTO trip_booking (trip_id, student_id, booking_date, booking_status)
VALUES
  (24, 1, NOW(), 'Confirmed'),
  (24, 3, NOW(), 'Confirmed'),
  (24, 4, NOW(), 'Confirmed'),
  (24, 5, NOW(), 'Confirmed'),
  (24, 6, NOW(), 'Confirmed');
```

This creates 5 confirmed bookings. When the boarding screen loads for driver ID 1
(Thabo), it will find trip 24 as active and show these 5 students.

---

## Testing

### Test 1: Get Active Trip

```bash
GET http://localhost:8080/api/shuttle-driver/1/active-trip
Authorization: Bearer shuttle-token-1
```

**Expected response (200 OK):**

```json
{
  "tripId": 24,
  "departureStop": "North Campus",
  "destinationStop": "South Campus",
  "departureTime": "17:33",
  "arrivalTime": null,
  "status": "SCHEDULED",
  "capacity": 15,
  "registrationNumber": "ABC 123 EC",
  "totalBooked": 5,
  "totalBoarded": 0
}
```

### Test 2: Get Booked Students

```bash
GET http://localhost:8080/api/shuttle-driver/trip/24/students
Authorization: Bearer shuttle-token-1
```

**Expected response (200 OK):**

```json
[
  {
    "bookingId": 1,
    "studentId": 1,
    "firstName": "Kelvin",
    "lastName": "Mudzingwa",
    "studentNumber": "S12345678",
    "bookingStatus": "Confirmed",
    "boardedAt": null
  },
  {
    "bookingId": 2,
    "studentId": 3,
    "firstName": "Alice",
    "lastName": "Johnson",
    "studentNumber": "20210001",
    "bookingStatus": "Confirmed",
    "boardedAt": null
  }
]
```

### Test 3: Mark Student as Boarded

```bash
POST http://localhost:8080/api/shuttle-driver/boarding/mark
Authorization: Bearer shuttle-token-1
Content-Type: application/json

{
  "bookingId": 1
}
```

**Expected response (200 OK):**

```json
{
  "success": true,
  "message": "Student marked as boarded successfully",
  "boardedAt": "17:35"
}
```

### Test 4: Verify Boarding Reflected in Student List

After marking student as boarded, call the students endpoint again:

```bash
GET http://localhost:8080/api/shuttle-driver/trip/24/students
```

The student with bookingId 1 should now have `"boardedAt": "17:35"` instead of `null`.

### Test 5: No Active Trip (driver with no scheduled trips)

```bash
GET http://localhost:8080/api/shuttle-driver/5/active-trip
```

**Expected response (404 Not Found):**

```
"No active trip assigned. Check your schedule."
```

---

## How the Android App Connects

```
┌────────────────────────────────────────────────────────────────────┐
│                     ANDROID BOARDING SCREEN                         │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  Screen opens (LaunchedEffect)                                     │
│       │                                                            │
│       ▼                                                            │
│  ViewModel.loadBoardingData()                                      │
│       │                                                            │
│       ├── GET /api/shuttle-driver/{driverId}/active-trip            │
│       │       Returns: trip details + capacity + counts            │
│       │                                                            │
│       ├── GET /api/shuttle-driver/trip/{tripId}/students            │
│       │       Returns: list of booked students + boarding status    │
│       │                                                            │
│       ▼                                                            │
│  UI shows: Trip header + student list with PENDING/BOARDED status  │
│                                                                    │
│                                                                    │
│  Driver taps "Mark as Boarded" button                              │
│       │                                                            │
│       ▼                                                            │
│  ViewModel.markStudentAsBoarded(bookingId)                         │
│       │                                                            │
│       ├── POST /api/shuttle-driver/boarding/mark                    │
│       │       Body: { "bookingId": 123 }                           │
│       │       Returns: { success, message, boardedAt }             │
│       │                                                            │
│       ▼                                                            │
│  UI updates: Student card shows "BOARDED" + timestamp              │
│              Boarded count increments                               │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

---

## Database Flow: Mark as Boarded

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ trip_booking │     │ boarding_log │     │   student    │
│              │     │              │     │              │
│ booking_id=1 │────▶│ booking_id=1 │     │ student_id=1 │
│ trip_id=24   │     │ boarded_at   │     │ Kelvin M.    │
│ student_id=1 │     │  = NOW()     │     │ S12345678    │
│ status=Conf. │     │              │     │              │
└──────────────┘     └──────────────┘     └──────────────┘
```

When "Mark as Boarded" is pressed:
1. Backend finds the `trip_booking` by `booking_id`
2. Backend checks if a `boarding_log` row exists for that `booking_id`
3. If not → creates a new row with `boarded_at = NOW()`
4. If yes → updates the existing row's `boarded_at = NOW()`
5. Returns success with the formatted time

---

## File Checklist

| # | File | Action | Done |
|---|------|--------|------|
| 1 | `entity/TripBooking.java` | Check exists → add missing fields | [ ] |
| 2 | `entity/Student.java` | Check exists → add missing fields | [ ] |
| 3 | `entity/BoardingLog.java` | Check exists → create new | [ ] |
| 4 | `repository/TripBookingRepository.java` | Create or add methods | [ ] |
| 5 | `repository/StudentRepository.java` | Check exists | [ ] |
| 6 | `repository/BoardingLogRepository.java` | Create new | [ ] |
| 7 | `repository/TripRepository.java` | Add `findActiveTrips` (if not from profile guide) | [ ] |
| 8 | `dto/ShuttleDriverDtos.java` | Add 4 inner classes | [ ] |
| 9 | `service/ShuttleDriverService.java` | Add 3 methods + inject 3 repositories | [ ] |
| 10 | `controller/ShuttleDriverController.java` | Add 3 endpoints | [ ] |
| 11 | Security config (if applicable) | Permit new endpoints | [ ] |
| 12 | Test data SQL | Insert bookings for testing | [ ] |

---

## Notes

- The boarding screen frontend already handles optimistic UI — when the driver taps
  "Mark as Boarded", the button shows a loading spinner and the card immediately
  updates to "BOARDED" status on success.
- If the backend call fails, the card stays as "PENDING" (no error popup needed).
- The `boardedAt` field is a formatted time string (e.g., "12:35") that the frontend
  displays below the check icon on boarded student cards.
- The `NoTrip` state on the frontend is triggered when the API returns 404 for
  `/active-trip`. This shows a friendly "No Active Trip" screen with a refresh button.
- `formatDateTime` uses "HH:mm" format. If you want full date+time, change the
  pattern to `"yyyy-MM-dd HH:mm"`.
