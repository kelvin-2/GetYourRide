# Backend Specification: Delete Driver Profile (Full Deletion)

## Overview

When a student driver presses "Delete Profile" on the Android app, the backend must **permanently remove** all data associated with that driver application. There is no longer a "deactivated" or soft-delete status. The record and all related data are deleted from the database.

---

## Application Status Values (Database)

The `application_status` column in the `driver_applications` table must only contain one of these 3 values:

| Status           | Description                                      |
|------------------|--------------------------------------------------|
| `APPROVED`       | Application reviewed and accepted by admin       |
| `REJECTED`       | Application reviewed and denied by admin         |
| `PENDING_REVIEW` | Application submitted, awaiting admin review     |

**Remove** any references to `DEACTIVATED`, `INACTIVE`, `PENDING_VERIFICATION`, or similar legacy statuses from the backend code and database constraints.

---

## Endpoint Changes

### `DELETE /api/driver-profile`

**Current behavior:** Soft-deletes (sets status to "DEACTIVATED")  
**New behavior:** Permanently deletes the driver profile and all associated data

#### What must be deleted (in order, respecting foreign keys):

1. **Driver documents** — Any uploaded files (driver's licence, vehicle registration) from the documents table AND from cloud storage (e.g., S3/Firebase Storage)
2. **Trip records** — All trips offered by this driver that are in `PENDING` or `SCHEDULED` status should be cancelled/deleted. Completed trips can be kept for audit or deleted based on business rules.
3. **Vehicle information** — The vehicle record tied to this driver application
4. **Driver application record** — The main `driver_applications` row
5. **User role reset** — The student's `role` field in the `users`/`students` table should be reset from `DRIVER`/`DRIVER_PENDING`/`DRIVER_APPROVED` back to `STUDENT` (the student account itself is NOT deleted, only the driver profile)

#### Request

```
DELETE /api/driver-profile
Authorization: Bearer <jwt_token>
```

No request body needed. The backend reads the driver/student ID from the JWT.

#### Response (200 OK)

```json
{
  "message": "Driver profile permanently deleted."
}
```

#### Error Responses

| Code | Body                                                       | When                                    |
|------|------------------------------------------------------------|-----------------------------------------|
| 401  | `{ "message": "Unauthorized" }`                            | Invalid or expired token                |
| 404  | `{ "message": "Driver profile not found." }`               | No driver application exists for user   |
| 500  | `{ "message": "Failed to delete driver profile." }`        | Internal server error during deletion   |

---

## Backend Implementation Steps

### 1. Update the `DriverApplicationStatus` enum/constants

```java
public enum ApplicationStatus {
    PENDING_REVIEW,
    APPROVED,
    REJECTED
}
```

Remove `DEACTIVATED`, `PENDING_VERIFICATION`, or any other legacy values.

### 2. Update the `DELETE /api/driver-profile` controller method

```java
@DeleteMapping("/api/driver-profile")
public ResponseEntity<?> deleteDriverProfile(@AuthenticationPrincipal UserDetails user) {
    driverProfileService.permanentlyDelete(user.getId());
    return ResponseEntity.ok(Map.of("message", "Driver profile permanently deleted."));
}
```

### 3. Create/update the service method

```java
@Transactional
public void permanentlyDelete(Long studentId) {
    DriverApplication application = driverApplicationRepository
        .findByStudentId(studentId)
        .orElseThrow(() -> new NotFoundException("Driver profile not found."));

    // 1. Delete uploaded documents from cloud storage
    documentStorageService.deleteAllForApplication(application.getId());

    // 2. Delete document records from DB
    driverDocumentRepository.deleteByApplicationId(application.getId());

    // 3. Cancel any pending/scheduled trips by this driver
    tripRepository.deleteByDriverIdAndStatusIn(
        studentId, 
        List.of("PENDING", "SCHEDULED")
    );

    // 4. Delete the driver application record (cascades to vehicle info if mapped)
    driverApplicationRepository.delete(application);

    // 5. Reset user role back to STUDENT
    Student student = studentRepository.findById(studentId).orElseThrow();
    student.setRole("STUDENT");
    studentRepository.save(student);
}
```

### 4. Database migration (if needed)

If your `application_status` column has a CHECK constraint or enum type, update it:

```sql
-- Remove old status values, keep only the 3 valid ones
ALTER TABLE driver_applications 
  DROP CONSTRAINT IF EXISTS chk_application_status;

ALTER TABLE driver_applications 
  ADD CONSTRAINT chk_application_status 
  CHECK (application_status IN ('PENDING_REVIEW', 'APPROVED', 'REJECTED'));

-- Clean up any existing DEACTIVATED records (optional — depends on business rules)
-- DELETE FROM driver_applications WHERE application_status = 'DEACTIVATED';
```

### 5. Update `GET /api/driver-profile` and `GET /api/driver-applications/status`

These endpoints should only return profiles/statuses with one of the 3 valid values. If the profile has been deleted, return 404.

The `applicationStatus` field in `DriverProfileResponse` should be one of:
- `"Pending Review"`
- `"Approved"`
- `"Rejected"`

---

## Summary of Changes

| Layer         | Change                                                                 |
|---------------|------------------------------------------------------------------------|
| Database      | Remove `DEACTIVATED` status, keep only `PENDING_REVIEW`, `APPROVED`, `REJECTED` |
| Entity/Enum   | Update `ApplicationStatus` enum to 3 values                            |
| Controller    | `DELETE /api/driver-profile` now does permanent deletion                |
| Service       | New `permanentlyDelete()` method that removes all related records       |
| Repository    | Add `deleteByApplicationId()` and `deleteByDriverIdAndStatusIn()` queries |
| Storage       | Delete uploaded files from cloud storage                                |
| User table    | Reset student role from DRIVER back to STUDENT after deletion           |
| Trips         | Cancel/delete any pending trips offered by this driver                  |
