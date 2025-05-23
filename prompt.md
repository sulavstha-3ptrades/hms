# Hall Booking Management System - Atomic Development Tasks

## 1. **Project Setup**

### 1.1 Initialize JavaFX Project

- Add the JavaFX and JPhonix dependencies with Maven.
- Configure `module-info.java` for modularity.

### 1.2 File Structure

- Create directories: `src/main/resources/com/group4/data` (for `.txt` files), `src/main/resources/com/group4/styles` (for `.css` files), `src/main/resources/com/group4/view` (for `.fxml` files), `src/main/java/com/group4/models`, `src/main/java/com/group4/services`, `src/main/java/com/group4/controllers`, `src/main/java/com/group4/utils`.

---

## 2. **Core Data Models**

### 2.1 User Class

- Fields: `userId`, `firstName`, `lastName`, `role`, `email`, `password`, `contactNumber`, `status`.
- Getters/setters, constructor.

### 2.2 Hall Class

- Fields: `hallId`, `type` (enum: AUDITORIUM, BANQUET, MEETING_ROOM), `capacity`, `ratePerHour`.

### 2.3 Booking Class

- Fields: `bookingId`, `customerId`, `hallId`, `startDateTime`, `endDateTime`, `totalCost`, `status`.

### 2.4 Issue Class

- Fields: `issueId`, `customerId`, `hallId`, `description`, `assignedStaffId`, `status`.

---

## 3. **File Handling**

### 3.1 File Path Constants

- Define paths: `USERS_FILE = "data/users.txt"`, `HALLS_FILE = "data/halls.txt"`, etc.

### 3.2 FileHandler Utility

- Methods: `readLines(String filePath)`, `writeLines(String filePath, List<String> lines)`.
- Use `BufferedReader`/`BufferedWriter` with `try-with-resources`.

### 3.3 Delimiter Standardization

- Use `|` as delimiter (e.g., `userID|name|role|email|hashedPassword|status`).

---

## 4. **Authentication Module**

### 4.1 Login Screen (FXML)

- UI: Email/password fields.
- Controller: Validate credentials against `users.txt`.

### 4.2 Registration Screen (FXML)

- UI: Name, email, password, confirm password fields.
- Controller: Hash password with BCrypt, write to `users.txt`.

### 4.3 Session Management

- Store active user in `SessionManager` singleton (e.g., `currentUserRole`, `userId`).

---

## 5. **Scheduler Features**

### 5.1 Hall CRUD Operations

- **Add Hall**: Form to input type/capacity/rate, append to `halls.txt`.
- **Edit Hall**: Load hall data by ID, update line in `halls.txt`.
- **Delete Hall**: Remove line from `halls.txt` after confirmation.

### 5.2 Availability Scheduling

- UI: Calendar picker for start/end datetime, remarks text area.
- Validation: Check for overlaps in `availability_schedule.txt`.

### 5.3 Maintenance Scheduling

- Reuse availability UI components; write to `maintenance_schedule.txt`.

---

## 6. **Customer Features**

### 6.1 Hall Search & Filter

- UI: TableView with filters (date, hall type, capacity).
- Load data from `halls.txt` and cross-reference `availability_schedule.txt`.

### 6.2 Booking Workflow

- **Step 1**: Select hall and datetime (validate against business hours 8 AM–6 PM).
- **Step 2**: Calculate cost (`ratePerHour * duration`).
- **Step 3**: Simulate payment (dummy button), generate receipt (JFrame).

### 6.3 Booking Cancellation

- Check if cancellation is >3 days before event; update `status` in `bookings.txt`.

---

## 7. **Administrator Features**

### 7.1 Staff Management

- CRUD for Scheduler staff: Add/edit/delete entries in `users.txt` with role="Scheduler".

### 7.2 Staff Management

- CRUD for Customer user: Add/edit/delete entries in `users.txt` with role="Customer".

### 7.3 User Blocking

- Toggle `status` field in `users.txt` (ACTIVE/BLOCKED).

---

## 8. **Manager Features**

### 8.1 Sales Dashboard

- Aggregate bookings from `bookings.txt` into weekly/monthly/yearly totals.
- Display with JavaFX `LineChart` or `BarChart`.

### 8.2 Issue Management

- Assign issues to staff: Update `assignedStaffId` and `status` in `issues.txt`.

---

## 9. **Validation & Error Handling**

### 9.1 Input Sanitization

- Regex for email: `^[A-Za-z0-9+_.-]+@(.+)$`.
- Date format: `DateTimeFormatter.ISO_LOCAL_DATE_TIME`.

### 9.2 Custom Exceptions

- `BookingConflictException`, `InvalidInputException` with user-friendly alerts.

---

## 10. **UI Components (Atomic)**

### 10.1 Navigation Bar

- Role-specific menus (e.g., Scheduler sees "Hall Management", Customer sees "My Bookings").

### 10.2 Receipt Generation

- Prefab JFrame with labels for `bookingId`, `hallId`, `totalCost`, `datetime`.

### 10.3 Responsive Tables

- Bind `TableView` columns to observable lists from `.txt` files.

---

## 11. **Security**

### 11.1 Password Hashing

- Integrate BCrypt: `BCrypt.hashpw(password, BCrypt.gensalt())`.

### 11.2 File Permissions

- Restrict write access to `halls.txt`/`users.txt` using `File.setWritable(false)` for non-admins.

---

## 12. **Testing**

### 12.1 Unit Tests

- `UserAuthenticationTest`: Verify login success/failure.
- `BookingCalculatorTest`: Validate cost calculation logic.

### 12.2 Integration Tests

- `EndToEndBookingTest`: Simulate customer booking flow with TestFX.

---
