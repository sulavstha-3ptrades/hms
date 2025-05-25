## **Customer Features**

### Hall Search & Filter

- UI: TableView with filters (date, hall type, capacity).
- Load data from `halls.txt` and cross-reference `availability_schedule.txt`.

### Booking Workflow

- **Step 1**: Select hall and datetime (validate against business hours 8 AM–6 PM).
- **Step 2**: Calculate cost (`ratePerHour * duration`).
- **Step 3**: Simulate payment (dummy button), generate receipt (JFrame).

### 6.3 Booking Cancellation

- Check if cancellation is >3 days before event; update `status` in `bookings.txt`.
