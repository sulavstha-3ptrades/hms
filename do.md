### 🎯 Objective

Refactor the `BookingConfirmationController` to act as a **dedicated dialog controller** for the booking confirmation flow, ensuring **clean separation of concerns** and **explicit data binding** between controllers.

---

### 🔧 Refactoring Steps

1. **Rename the FXML File**

   - Rename `BookingConfirmation.fxml` → `BookingConfirmationDialog.fxml` to clearly indicate that it represents a dialog.

2. **Make `BookingConfirmationController` Dialog-Specific**
   - Scope the controller **only** to handle dialog UI logic and field bindings.
   - Define `@FXML` bindings for the dialog elements:
     - `Label hallNameLabel`
     - `Label dateLabel`
     - `Label timeLabel`
     - `Label durationLabel`
     - `Label costLabel`
     - `TextArea specialRequests`

---

### 🔁 Data Flow Integration

In `HallBookingController.java`:

```java
FXMLLoader loader = new FXMLLoader(fxmlUrl);
Parent dialogRoot = loader.load();

BookingConfirmationController dialogController = loader.getController();
if (dialogController == null) {
    showAlert("Error: Could not initialize the booking confirmation dialog controller.");
    return;
}

// Set booking details
dialogController.setBookingDetails(
    selectedHall,
    bookingDate,
    LocalTime.now(),
    LocalTime.now().plusHours(1),
    50.0
);

// Show the dialog
Dialog<ButtonType> bookingDialog = new Dialog<>();
bookingDialog.setDialogPane((DialogPane) dialogRoot);
bookingDialog.setTitle("Book Hall - " + selectedHall.getHallId());

Optional<ButtonType> result = bookingDialog.showAndWait();
if (result.isPresent() && result.get().getButtonData() == ButtonType.OK_DONE.getButtonData()) {
    System.out.println("Booking Confirmed!");
    String specialReqs = dialogController.getSpecialRequests();
    // Process booking with specialReqs and other details
} else {
    System.out.println("Booking Cancelled.");
}

loadHalls(); // Refresh the hall list
```
