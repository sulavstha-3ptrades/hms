# Hall Management System

A JavaFX-based application for managing event hall bookings and maintenance.

## Prerequisites

- Java 21 or higher
- Maven

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd hall_management_system
   ```

2. Build and run the application:
   ```bash
   mvn clean install
   mvn javafx:run
   ```

   The application will automatically create the required `data` directory and files with appropriate permissions on first run.

## File Permissions

The application automatically handles setting up the `data/` directory and files with appropriate permissions. On first run, it will:
- Create the `data` directory
- Create all necessary data files
- Set appropriate permissions (755 for directory, 644 for files)

If you encounter any permission issues, you can manually set the permissions using:

```bash
chmod -R 755 data/
chmod 644 data/*.txt
```

## Project Structure

- `src/` - Source code
- `data/` - Data storage (auto-created by setup script)
  - `bookings.txt` - Booking records
  - `halls.txt` - Hall information
  - `users.txt` - User accounts
  - `issues.txt` - Maintenance issues
  - `availability_schedule.txt` - Hall availability
  - `maintenance_schedule.txt` - Maintenance schedule

## Troubleshooting

If you encounter permission issues:
1. Ensure the `data/` directory exists
2. Verify write permissions on the `data/` directory
3. Run the setup script again if needed
